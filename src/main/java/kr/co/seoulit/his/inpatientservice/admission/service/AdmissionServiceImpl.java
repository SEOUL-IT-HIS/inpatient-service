package kr.co.seoulit.his.inpatientservice.admission.service;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.admission.mapper.AdmissionMapper;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.service.BedAssignmentService;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import kr.co.seoulit.his.inpatientservice.admission.event.DischargeRequestedEvent;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AdmissionServiceImpl implements AdmissionService {
    private final AdmissionRepository admissionRepository;
    private final AdmissionMapper admissionMapper;
    private final BedAssignmentService bedAssignmentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // false로 두면(app.kafka.enabled=false) 카프카 브로커가 안 켜져 있어도
    // 퇴원신청 처리 자체는 그대로 되고, 이벤트 발행만 건너뜀
    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled;


    @Override
    public AdmissionDTO receiveAdmission(AdmissionDTO requestDto){
        validateNoActiveAdmission(requestDto.getPatientId());
        AdmissionEntity entity = admissionMapper.toEntity(requestDto);
        entity.setAdmissionId(generateNextAdmissionId());
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
        validateNoActiveAdmission(requestDto.getPatientId());
        AdmissionEntity entity = admissionMapper.toEntity(requestDto);
        entity.setAdmissionId(generateNextAdmissionId());
        return admissionMapper.toDto(admissionRepository.save(entity));
    }

    // "A001", "A002" ... 형식과 이어지도록 현재 최대 번호 다음 값을 생성
    private String generateNextAdmissionId() {
        int maxSeq = admissionRepository.findAll().stream()
                .map(AdmissionEntity::getAdmissionId)
                .filter(id -> id != null && id.matches("A\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(1)))
                .max()
                .orElse(0);
        return String.format("A%03d", maxSeq + 1);
    }
    private void validateNoActiveAdmission(String patientId) {
        if (admissionRepository.existsByPatientIdAndStatusNot(patientId, "DISCHARGED")) {
            throw new BusinessException(ErrorCode.ADMISSION_ALREADY_ACTIVE);
        }
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
                                BedAssignmentService bedAssignmentService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.admissionRepository = admissionRepository;
        this.admissionMapper = admissionMapper;
        this.bedAssignmentService = bedAssignmentService;
        this.kafkaTemplate = kafkaTemplate;
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
    if ("DISCHARGE_REQUESTED".equals(status) && kafkaEnabled) {
        kafkaTemplate.send("discharge.requested", admissionId,
                DischargeRequestedEvent.of(updated.getPatientId(), updated.getAdmissionId()));
        String roomTypeCode = bedAssignmentService.findActiveRoomTypeCode(admissionId);
        long days = calculateAdmissionDays(updated);
        kafkaTemplate.send("inpatient-billing-charge", admissionId,
                DischargeRequestedEvent.roomFee(updated.getPatientId(),admissionId,roomTypeCode,days));
    }
    return admissionMapper.toDto(updated);
}
    private long calculateAdmissionDays(AdmissionEntity entity) {
        return ChronoUnit.DAYS.between(entity.getAdmissionDate().toLocalDate(), LocalDate.now());
    }


}
