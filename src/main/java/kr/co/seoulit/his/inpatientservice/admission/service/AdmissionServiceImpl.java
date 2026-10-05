package kr.co.seoulit.his.inpatientservice.admission.service;

import jakarta.transaction.Transactional;
import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.admission.event.BillingChargeEvent;
import kr.co.seoulit.his.inpatientservice.admission.event.AdmissionRequestedEvent;
import kr.co.seoulit.his.inpatientservice.admission.mapper.AdmissionMapper;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.service.BedAssignmentService;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.common.util.DateRules;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class AdmissionServiceImpl implements AdmissionService {
    // 병실 타입(roomTypeCode) -> 수납 수가코드 매핑. billing_master에 등록된 코드(FEE011/012/013).
    // ROOM_TYPE_CD 그룹(52f3d558-c70b-47c3-a4d9-982b64d37de8): 01=Private Room(1인실), 02=Multi-bed Room(다인실),
    // 03=Isolation Room(격리실), 04=VIP Room(특실)
    // - 03 격리실은 공통코드는 사용중이지만 수가코드가 아직 없어 매핑 제외 → 격리실 병상으로 퇴원신청하면 ROOM_TYPE_FEE_CODE_NOT_MAPPED
    // "일반병실"(FEE009)은 공통코드에 별도 구분이 없어 수납팀에 요청해 fee 목록에서 제외함
    private static final Map<String, String> ROOM_TYPE_FEE_CODE_MAP = Map.of(
            "01", "FEE011", // 1인실 (비급여, 200,000원)
            "02", "FEE012", // 다인실 (급여, 50,000원)
            "04", "FEE013"  // 특실 VIP (비급여, 500,000원)
    );

    private final AdmissionRepository admissionRepository;
    private final AdmissionMapper admissionMapper;
    private final BedAssignmentService bedAssignmentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String billingChargeTopic;
    private final boolean kafkaEnabled;

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

    // [사용 중지] 호출하는 곳 없음 (createAdmission과 동일 로직) — AdmissionController의 /reception 주석 참고
    // @Override
    // public AdmissionDTO receiveAdmission(AdmissionDTO requestDto){
    //     validateNoActiveAdmission(requestDto.getPatientId());
    //     AdmissionEntity entity = admissionMapper.toEntity(requestDto);
    //     entity.setAdmissionId(generateNextAdmissionId());
    //     return admissionMapper.toDto(admissionRepository.save(entity));
    // }

    @Override
    public List<AdmissionDTO> getAdmissions() {
        return admissionRepository.findAll().stream()
                .map(admissionMapper::toDto)
                .toList();
    }

    @Override
    public AdmissionDTO createAdmission(AdmissionDTO requestDto) {
        validateAdmissionDate(requestDto.getAdmissionDate());
        validateNoActiveAdmission(requestDto.getPatientId(), null);
        AdmissionEntity entity = admissionMapper.toEntity(requestDto);
        entity.setAdmissionId(generateNextAdmissionId());
        // 비어 있으면 기본값 — 상태가 없으면 진행중 입원으로 안 보이고, 입원일이 없으면 퇴원신청 때 입원일수 계산이 실패함
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus(AdmissionRequestedEvent.INITIAL_STATUS);
        }
        if (entity.getAdmissionDate() == null) {
            entity.setAdmissionDate(DateRules.now());
        }
        return admissionMapper.toDto(admissionRepository.save(entity));
    }

    // 입원일은 미래일 수 없음 (시계 오차 5분 여유) — 비어 있으면 등록 시 지금으로 채우므로 여기선 통과
    private void validateAdmissionDate(LocalDateTime admissionDate) {
        if (admissionDate != null && DateRules.isFuture(admissionDate)) {
            throw new BusinessException(ErrorCode.ADMISSION_DATE_INVALID);
        }
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
    // excludeAdmissionId: 수정할 때 자기 자신은 빼고 확인 (등록일 때는 null)
    private void validateNoActiveAdmission(String patientId, String excludeAdmissionId) {
        if (admissionRepository.existsActiveAdmission(patientId, excludeAdmissionId)) {
            throw new BusinessException(ErrorCode.ADMISSION_ALREADY_ACTIVE);
        }
    }


    @Override
    public AdmissionDTO getAdmission(String admissionId) {
        AdmissionEntity entity = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMISSION_NOT_FOUND));
        return admissionMapper.toDto(entity);
    }

    @Override
    public AdmissionDTO changeDoctor(String admissionId, String doctorId) {
        if (doctorId == null || doctorId.isBlank()) {
            throw new BusinessException(ErrorCode.DOCTOR_ID_REQUIRED);
        }
        AdmissionEntity entity = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMISSION_NOT_FOUND));
        if ("DISCHARGED".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.ADMISSION_ALREADY_DISCHARGED);
        }
        // 담당의만 바꿈 — updateAdmission(PUT)은 환자/입원일/상태까지 덮어써서 담당의 변경에는 쓰지 않음
        // requestedBy(입원을 요청한 응급 의사)는 기록용이라 그대로 둠
        entity.setDoctorId(doctorId.trim());
        return admissionMapper.toDto(admissionRepository.save(entity));
    }

    @Override
    public AdmissionDTO updateAdmission(String admissionId, AdmissionDTO requestDto) {
        AdmissionEntity entity = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMISSION_NOT_FOUND));

        // 빈 값은 기존 값 유지 (상태를 비우면 진행중 입원으로 안 보여 중복 검사를 피해 감)
        String patientId = requestDto.getPatientId() != null ? requestDto.getPatientId() : entity.getPatientId();
        String status = requestDto.getStatus() != null && !requestDto.getStatus().isBlank()
                ? requestDto.getStatus() : entity.getStatus();
        LocalDateTime admissionDate = requestDto.getAdmissionDate() != null
                ? requestDto.getAdmissionDate() : entity.getAdmissionDate();

        validateAdmissionDate(admissionDate);
        // 수정 결과가 "진행중 입원"이면 같은 환자의 다른 진행중 입원이 없는지 다시 확인
        // (환자를 바꾸거나, 퇴원 건을 다시 활성 상태로 되돌려 중복을 만드는 우회 방지)
        if (!"DISCHARGED".equals(status)) {
            validateNoActiveAdmission(patientId, admissionId);
        }

        entity.setPatientId(patientId);
        entity.setAdmissionDate(admissionDate);
        entity.setStatus(status);
        return admissionMapper.toDto(admissionRepository.save(entity));
    }

    // @Transactional: 입원 상태 저장 + 배정 퇴상 + 병상 EMPTY를 하나로 묶음
    // (병상 해제 중 실패하면 상태도 롤백 → "환자는 DISCHARGED인데 병상은 OCCUPIED"로 남지 않음)
    @Transactional
    @Override
    public AdmissionDTO changeStatus(String admissionId, String status){
        AdmissionEntity entity = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMISSION_NOT_FOUND));

        if ("DISCHARGE_REQUESTED".equals(status) && isDischargeRequestedOrDischarged(entity.getStatus())) {
            throw new BusinessException(ErrorCode.DISCHARGE_ALREADY_REQUESTED);
        }

        // 퇴원신청이면 입원료 청구 이벤트를 상태 저장 "전에" 먼저 만들어 둠
        // (병상배정 없음/병실유형 미매핑으로 실패하면 상태도 안 바뀌고 이벤트도 안 나가야 다시 시도할 수 있음)
        BillingChargeEvent roomFeeEvent = null;
        if ("DISCHARGE_REQUESTED".equals(status) && kafkaEnabled) {
            roomFeeEvent = buildRoomFeeEvent(entity);
        }

        entity.setStatus(status);
        AdmissionEntity updated = admissionRepository.save(entity);

        if ("DISCHARGED".equals(status)) {
            bedAssignmentService.releaseBedByAdmissionId(admissionId);
        }

        if (roomFeeEvent != null) {
            publishDischargeBillingEvents(updated, roomFeeEvent);
        }

        return admissionMapper.toDto(updated);
    }

    private boolean isDischargeRequestedOrDischarged(String currentStatus) {
        return "DISCHARGE_REQUESTED".equals(currentStatus) || "DISCHARGED".equals(currentStatus);
    }

    // 퇴원신청 시점에 수납서비스로 보낼 이벤트 2건 발행: (1) 퇴원신청 신호, (2) 입원료 청구
    // - 트랜잭션이 "커밋된 뒤"에 보냄: 상태 저장이 롤백되면 청구 이벤트도 나가지 않아야 함 (병상배정 회신과 같은 방식)
    private void publishDischargeBillingEvents(AdmissionEntity admission, BillingChargeEvent roomFeeEvent) {
        String admissionId = admission.getAdmissionId();
        BillingChargeEvent dischargeRequestEvent = BillingChargeEvent.dischargeRequest(admission.getPatientId(), admissionId);

        Runnable send = () -> {
            kafkaTemplate.send(billingChargeTopic, admissionId, dischargeRequestEvent);
            kafkaTemplate.send(billingChargeTopic, admissionId, roomFeeEvent);
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
        } else {
            // 트랜잭션 밖에서 호출된 경우(정상 흐름에선 없음) — 바로 보냄
            send.run();
        }
    }

    // 입원료 청구 이벤트 생성 — 활성 병상배정이 없으면 BED_ASSIGNMENT_NOT_FOUND, 병실유형이 매핑에 없으면 ROOM_TYPE_FEE_CODE_NOT_MAPPED
    private BillingChargeEvent buildRoomFeeEvent(AdmissionEntity admission) {
        String admissionId = admission.getAdmissionId();
        String roomTypeCode = bedAssignmentService.findRoomTypeCodeByAdmissionId(admissionId);
        String feeCode = ROOM_TYPE_FEE_CODE_MAP.get(roomTypeCode);
        if (feeCode == null) {
            throw new BusinessException(ErrorCode.ROOM_TYPE_FEE_CODE_NOT_MAPPED);
        }
        // 입원일수는 날짜(자정) 기준으로 세고, 당일 입·퇴원도 최소 1일로 청구
        // (만 24시간 기준으로 세면 하루가 안 된 입원이 0일이 되어 수납의 수량 제약(quantity > 0)에 걸려 청구가 실패함)
        long stayDays = Math.max(1,
                ChronoUnit.DAYS.between(admission.getAdmissionDate().toLocalDate(), DateRules.today()));
        return BillingChargeEvent.roomFee(admission.getPatientId(), admissionId, feeCode, stayDays);
    }

}
