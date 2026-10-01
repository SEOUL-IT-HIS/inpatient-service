package kr.co.seoulit.his.inpatientservice.prescription.service;

import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.prescription.entity.PrescriptionEntity;
import kr.co.seoulit.his.inpatientservice.prescription.entity.PrescriptionItemEntity;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionItemDTO;
import kr.co.seoulit.his.inpatientservice.prescription.client.PrescriptionCoreClient;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionCreateDTO;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionDTO;
import kr.co.seoulit.his.inpatientservice.prescription.event.LabResultReportedEvent;
import kr.co.seoulit.his.inpatientservice.prescription.mapper.PrescriptionMapper;
import kr.co.seoulit.his.inpatientservice.prescription.repository.PrescriptionItemRepository;
import kr.co.seoulit.his.inpatientservice.prescription.repository.PrescriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrescriptionServiceImpl implements PrescriptionService{
    // 외래 처방코어 규칙: items[].prescriptionType은 반드시 이 한글 문자열이어야 전송 로직이 인식함
    public static final String TYPE_LAB = "검사";
    public static final String TYPE_MEDICATION = "약품";

    // 병동이 관리하는 항목 전송 상태 / 처방 상태 값
    public static final String SEND_SENT = "SENT";
    public static final String SEND_FAILED = "SEND_FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private final PrescriptionCoreClient prescriptionCoreClient;
    private final PrescriptionRepository prescriptionRepository;
    private final AdmissionRepository admissionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PrescriptionMapper prescriptionMapper;

    public PrescriptionServiceImpl(PrescriptionCoreClient prescriptionCoreClient, PrescriptionRepository prescriptionRepository, AdmissionRepository admissionRepository, PrescriptionItemRepository prescriptionItemRepository, PrescriptionMapper prescriptionMapper) {
        this.prescriptionCoreClient = prescriptionCoreClient;
        this.prescriptionRepository = prescriptionRepository;
        this.admissionRepository = admissionRepository;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.prescriptionMapper = prescriptionMapper;
    }

    @Override
    public PrescriptionDTO createPrescription(String admissionId, PrescriptionCreateDTO requestDto) {
        AdmissionEntity admission = admissionRepository.findById(admissionId)
                .orElseThrow(()->new BusinessException(ErrorCode.ADMISSION_NOT_FOUND));
        // 처방의사(prescribedBy)는 외래 필수값 — 담당의가 없으면 외래까지 보내지 않고 바로 알려줌
        // (예: 응급 요청에 requestedBy가 빠져서 들어온 입원 건 → 입원 상세에서 담당의를 지정하면 해결)
        if (admission.getDoctorId() == null || admission.getDoctorId().isBlank()) {
            throw new BusinessException(ErrorCode.ATTENDING_DOCTOR_NOT_ASSIGNED);
        }
        requestDto.setPatientId(admission.getPatientId());
        requestDto.setPrescribedBy(admission.getDoctorId());
        requestDto.setDepartmentCode(admission.getAdmissionDeptId()); // 외래 약제 전송에서 입원 경로로 사용
        PrescriptionDTO created = prescriptionCoreClient.createPrescription(admissionId, requestDto);
        created.setAdmissionId(admissionId);

        prescriptionRepository.save(prescriptionMapper.toEntity(created));

        if(created.getItems() != null){
            for(PrescriptionItemDTO itemDTO : created.getItems()) {
                itemDTO.setPrescriptionId(created.getPrescriptionId());
                prescriptionItemRepository.save(prescriptionMapper.toItemEntity(itemDTO));
            }
        }

        // 외래는 자동 전송을 하지 않으므로, 등록 직후 병동이 항목 종류별로 검사실/약제부 전송을 호출
        // 전송이 실패해도 처방 등록 자체는 이미 외래에 완료된 것이라 되돌리지 않고, 항목에 SEND_FAILED로 남겨 재전송할 수 있게 함
        dispatchPending(created.getPrescriptionId());
        return getPrescription(created.getPrescriptionId());
    }

    @Override
    public List<PrescriptionDTO> getPrescriptionsByAdmission(String admissionId) {
        return prescriptionRepository.findByAdmissionId(admissionId).stream()
                .map(this::toDtoWithItems)
                .toList();
    }

    @Override
    public PrescriptionDTO getPrescription(String prescriptionId) {
        PrescriptionEntity entity = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(()-> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        return toDtoWithItems(entity);
    }

    @Override
    public PrescriptionDTO retryDispatch(String prescriptionId) {
        PrescriptionEntity entity = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        if (STATUS_CANCELLED.equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_ALREADY_CANCELLED);
        }
        dispatchPending(prescriptionId);
        return getPrescription(prescriptionId);
    }

    @Override
    public PrescriptionDTO cancelPrescription(String prescriptionId, String cancelReason) {
        PrescriptionEntity entity = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        if (STATUS_CANCELLED.equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_ALREADY_CANCELLED);
        }

        // 외래 처방코어에 먼저 취소 요청 — 실패하면 예외가 그대로 올라가서 병동 DB도 바뀌지 않음
        // userId(취소 처리자)는 처방의사(입원 건 주치의)로 넘김 — 서버 간 호출이라 로그인 사용자를 알 수 없음
        prescriptionCoreClient.deactivate(prescriptionId, cancelReason, entity.getPrescribedBy());

        entity.setStatus(STATUS_CANCELLED);
        entity.setCancelledAt(LocalDateTime.now());
        entity.setCancelReason(cancelReason);
        prescriptionRepository.save(entity);
        return getPrescription(prescriptionId);
    }

    @Override
    public void applyLabResult(LabResultReportedEvent event) {
        LabResultReportedEvent.Data data = event.data();
        if (data == null || data.prescriptionId() == null || data.items() == null) {
            log.warn("lab result without prescriptionId/items ignored: eventId={}", event.eventId());
            return;
        }
        // 외래 처방 등 병동 DB에 없는 처방의 결과는 우리 것이 아니므로 무시
        if (!prescriptionRepository.existsById(data.prescriptionId())) {
            return;
        }

        LocalDateTime reportedAt = parseDateTime(data.reportedAt());
        for (LabResultReportedEvent.Item item : data.items()) {
            List<PrescriptionItemEntity> targets =
                    prescriptionItemRepository.findByPrescriptionIdAndItemCode(data.prescriptionId(), item.itemCode());
            for (PrescriptionItemEntity target : targets) {
                target.setLabOrderId(data.labOrderId());
                target.setResultStatus(data.resultStatus());
                target.setResultReportedAt(reportedAt);
                target.setResultSummary(summarize(item.details()));
                prescriptionItemRepository.save(target);
            }
            log.info("lab result applied: prescriptionId={} itemCode={} status={} matchedItems={}",
                    data.prescriptionId(), item.itemCode(), data.resultStatus(), targets.size());
        }
    }

    // 아직 전송되지 않은(SENT가 아닌) 항목을 종류별로 묶어서 전송 — 외래 API가 처방 단위라 종류당 한 번씩 호출
    private void dispatchPending(String prescriptionId) {
        List<PrescriptionItemEntity> pending = prescriptionItemRepository.findByPrescriptionId(prescriptionId).stream()
                .filter(item -> !SEND_SENT.equals(item.getSendStatus()))
                .toList();

        dispatchType(prescriptionId, pending, TYPE_LAB, () -> prescriptionCoreClient.dispatchLab(prescriptionId));
        dispatchType(prescriptionId, pending, TYPE_MEDICATION, () -> prescriptionCoreClient.dispatchPharmacy(prescriptionId));
    }

    private void dispatchType(String prescriptionId, List<PrescriptionItemEntity> pending, String type, Runnable dispatch) {
        List<PrescriptionItemEntity> targets = pending.stream()
                .filter(item -> type.equals(item.getPrescriptionType()))
                .toList();
        if (targets.isEmpty()) {
            return;
        }

        String result;
        try {
            dispatch.run();
            result = SEND_SENT;
        } catch (BusinessException e) {
            // 예: 약제 쪽이 준비 안 됨, 외래 일시 장애 — 등록은 유지하고 재전송 버튼으로 다시 보낼 수 있게 남김
            log.warn("prescription dispatch failed: prescriptionId={} type={} reason={}", prescriptionId, type, e.getMessage());
            result = SEND_FAILED;
        }

        LocalDateTime now = LocalDateTime.now();
        for (PrescriptionItemEntity item : targets) {
            item.setSendStatus(result);
            if (SEND_SENT.equals(result)) {
                item.setSentAt(now);
            }
            prescriptionItemRepository.save(item);
        }
    }

    // 검사 결과 상세를 한 줄 요약으로 — 예: "02: 6.2 x10^3/uL (4.0-10.0) [H]"
    private String summarize(List<LabResultReportedEvent.Detail> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }
        String summary = details.stream()
                .map(d -> {
                    StringBuilder sb = new StringBuilder();
                    if (d.detailCode() != null) sb.append(d.detailCode()).append(": ");
                    sb.append(d.resultValue() == null ? "-" : d.resultValue());
                    if (d.unit() != null) sb.append(' ').append(d.unit());
                    if (d.referenceRange() != null) sb.append(" (").append(d.referenceRange()).append(')');
                    if (d.abnormalFlag() != null && !"N".equals(d.abnormalFlag())) sb.append(" [").append(d.abnormalFlag()).append(']');
                    return sb.toString();
                })
                .collect(Collectors.joining(", "));
        return summary.length() > 1000 ? summary.substring(0, 1000) : summary; // 칼럼 길이(1000) 초과 방지
    }

    // "2026-10-01T10:00:00+09:00" → LocalDateTime (형식이 다르면 null — 결과 저장 자체는 진행)
    private LocalDateTime parseDateTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private PrescriptionDTO toDtoWithItems(PrescriptionEntity entity){
        PrescriptionDTO dto = prescriptionMapper.toDto(entity);
        dto.setItems(prescriptionItemRepository.findByPrescriptionId(entity.getPrescriptionId()).stream()
                .map(prescriptionMapper::toItemDto)
                .toList());
        return dto;
    }

}
