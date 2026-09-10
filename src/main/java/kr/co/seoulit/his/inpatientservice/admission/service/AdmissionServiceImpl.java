package kr.co.seoulit.his.inpatientservice.admission.service;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.admission.event.BillingChargeEvent;
import kr.co.seoulit.his.inpatientservice.admission.mapper.AdmissionMapper;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.service.BedAssignmentService;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class AdmissionServiceImpl implements AdmissionService {
    // 병실 타입(roomTypeCode) -> 수납 수가코드 매핑. billing_master에 이미 등록된 코드(FEE011/012).
    // ROOM_TYPE_CD 그룹(52f3d558-c70b-47c3-a4d9-982b64d37de8): 01=1인실, 02=다인실, 03=격리실(USE_YN=N, 비활성이라 매핑 제외)
    // "일반병실"(FEE009)은 공통코드에 별도 구분이 없어 수납팀에 요청해 fee 목록에서 제외함 — 병동은 1인실/다인실 2종만 청구
    private static final Map<String, String> ROOM_TYPE_FEE_CODE_MAP = Map.of(
            "01", "FEE011", // 1인실
            "02", "FEE012"  // 다인실
    );

    private final AdmissionRepository admissionRepository;
    private final AdmissionMapper admissionMapper;
    private final BedAssignmentService bedAssignmentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String billingChargeTopic;
    private final boolean kafkaEnabled;

    @Override
    public AdmissionDTO receiveAdmission(AdmissionDTO requestDto){
        AdmissionEntity entity = admissionMapper.toEntity(requestDto);
        return admissionMapper.toDto(admissionRepository.save(entity));
    }

    @Override
    public List<AdmissionDTO> getAdmissions() {
        return admissionRepository.findAll().stream()
                .map(admissionMapper::toDto)
                .toList();
    }

    @Override
    public AdmissionDTO createAdmission(AdmissionDTO requestDto) {
        AdmissionEntity entity = admissionMapper.toEntity(requestDto);
        return admissionMapper.toDto(admissionRepository.save(entity));
    }

    @Override
    public AdmissionDTO getAdmission(String admissionId) {
        AdmissionEntity entity = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        return admissionMapper.toDto(entity);
    }

    @Override
    public AdmissionDTO updateAdmission(String admissionId, AdmissionDTO requestDto) {
        AdmissionEntity entity = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));

        entity.setPatientId(requestDto.getPatientId());
        entity.setAdmissionDate(requestDto.getAdmissionDate());
        entity.setStatus(requestDto.getStatus());
        return admissionMapper.toDto(admissionRepository.save(entity));
    }
public AdmissionServiceImpl(AdmissionRepository admissionRepository, AdmissionMapper admissionMapper,
        BedAssignmentService bedAssignmentService, KafkaTemplate<String, Object> kafkaTemplate,
        @Value("${billing.charge.topic.inpatient}") String billingChargeTopic,
        @Value("${app.kafka.enabled}") boolean kafkaEnabled) {
    this.admissionRepository = admissionRepository;
    this.admissionMapper = admissionMapper;
    this.bedAssignmentService = bedAssignmentService;
    this.kafkaTemplate = kafkaTemplate;
    this.billingChargeTopic = billingChargeTopic;
    this.kafkaEnabled = kafkaEnabled;
}

@Override
public AdmissionDTO changeStatus(String admissionId, String status){
    AdmissionEntity entity = admissionRepository.findById(admissionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));

    entity.setStatus(status);
    AdmissionEntity updated = admissionRepository.save(entity);

    if ("DISCHARGED".equals(status)) {
        bedAssignmentService.releaseBedByAdmissionId(admissionId);
    }

    if ("DISCHARGE_REQUESTED".equals(status)) {
        publishDischargeBillingEvents(updated);
    }

    return admissionMapper.toDto(updated);
}

// 퇴원신청 시점에 수납서비스로 보낼 이벤트 2건 발행: (1) 퇴원신청 신호, (2) 입원료 청구
private void publishDischargeBillingEvents(AdmissionEntity admission) {
    if (!kafkaEnabled) {
        return;
    }

    String admissionId = admission.getAdmissionId();
    String patientId = admission.getPatientId();

    kafkaTemplate.send(billingChargeTopic, admissionId,
            BillingChargeEvent.dischargeRequest(patientId, admissionId));

    String roomTypeCode = bedAssignmentService.findRoomTypeCodeByAdmissionId(admissionId);
    String feeCode = ROOM_TYPE_FEE_CODE_MAP.get(roomTypeCode);
    if (feeCode == null) {
        throw new BusinessException(ErrorCode.ROOM_TYPE_FEE_CODE_NOT_MAPPED);
    }
    long stayDays = ChronoUnit.DAYS.between(admission.getAdmissionDate(), LocalDateTime.now());

    kafkaTemplate.send(billingChargeTopic, admissionId,
            BillingChargeEvent.roomFee(patientId, admissionId, feeCode, stayDays));
}

}
