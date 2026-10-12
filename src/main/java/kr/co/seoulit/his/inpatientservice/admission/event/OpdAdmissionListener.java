package kr.co.seoulit.his.inpatientservice.admission.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.admission.service.AdmissionService;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.util.DateRules;
import lombok.extern.slf4j.Slf4j;

/**
 * 외래 입원요청 수신 (opd.admission.requested.v1)
 * - 요청: 입원 건(REQUESTED, 경로 Outpatient)을 만들면 입퇴원관리 > Admission Requests 목록에 바로 나타남
 *         만들 수 없으면(이미 입원 중인 환자, 필수값 누락 등) 사유와 함께 AdmissionRejected 를 외래에 자동 회신
 *         (병동 직원이 직접 거절하는 기능은 없음)
 * - 병상 배정 회신은 BedAssignmentServiceImpl 에서 보냄
 * - 외래 requestId는 admissionRequestId 컬럼에 저장 (dispositionId는 응급 전용이라 null)
 * - 취소·수정은 받지 않음 (외래와 합의 — 응급과 같이 요청과 결과 회신만 연동)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OpdAdmissionListener {

    private static final int NOTE_MAX_LENGTH = 500; // AdmissionEntity.note 컬럼 길이

    private final AdmissionService admissionService;
    private final AdmissionRepository admissionRepository;
    private final OpdAdmissionResultPublisher resultPublisher;

    public OpdAdmissionListener(AdmissionService admissionService, AdmissionRepository admissionRepository,
                                OpdAdmissionResultPublisher resultPublisher) {
        this.admissionService = admissionService;
        this.admissionRepository = admissionRepository;
        this.resultPublisher = resultPublisher;
    }

    // properties 설명은 AdmissionRequestedListener와 동일 (이 리스너 전용 타입으로 읽고, 형식이 깨진 메시지는 건너뜀)
    @KafkaListener(
            topics = "${outpatient.admission.requested.topic:opd.admission.requested.v1}",
            groupId = "inpatient-service",
            properties = {
                    "value.deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer",
                    "spring.deserializer.value.delegate.class=org.springframework.kafka.support.serializer.JsonDeserializer",
                    "spring.json.value.default.type=kr.co.seoulit.his.inpatientservice.admission.event.OpdAdmissionRequestedEvent"
            })
    public void onAdmissionRequested(OpdAdmissionRequestedEvent event) {
        OpdAdmissionRequestedEvent.Data data = event.data();
        // 회신할 키(requestId)가 없으면 처리할 수 없음 — 로그만 남기고 버림
        if (data == null || isBlank(data.requestId())) {
            log.warn("opd admission request without requestId ignored: {}", event);
            return;
        }
        String requestId = data.requestId();
        log.info("opd admission request received: requestId={} patientId={}", requestId, data.patientId());

        // 같은 요청이 다시 오면(외래 재발행 등) 입원 건을 두 번 만들지 않도록 무시
        if (admissionRepository.existsByAdmissionRequestIdAndAdmissionRoute(requestId, OpdAdmissionRequestedEvent.ADMISSION_ROUTE)) {
            log.info("duplicate opd admission request ignored: requestId={}", requestId);
            return;
        }

        // 필수값 확인 — 입원 건을 만들 수 없으므로 바로 거절 회신
        if (isBlank(data.patientId())) {
            reject(requestId, "patientId is required");
            return;
        }
        if (isBlank(data.targetDeptCode())) {
            reject(requestId, "targetDeptCode is required");
            return;
        }
        if (data.note() != null && data.note().length() > NOTE_MAX_LENGTH) {
            reject(requestId, "note must be 500 characters or less");
            return;
        }

        LocalDateTime requestedAt;
        LocalDate preferredAdmitDate;
        try {
            requestedAt = toHospitalTime(event.occurredAt());
            preferredAdmitDate = isBlank(data.preferredAdmitDate()) ? null : LocalDate.parse(data.preferredAdmitDate());
        } catch (DateTimeParseException e) {
            reject(requestId, "occurredAt or preferredAdmitDate format is invalid");
            return;
        }

        try {
            // 입원 건 생성은 기존 로직 그대로 재사용 — 진행중 입원 중복 확인, 미래 입원일 확인, 입원 ID(A001...) 생성 포함
            AdmissionDTO created = admissionService.createAdmission(AdmissionDTO.builder()
                    .patientId(data.patientId())
                    .doctorId(data.requestedBy())   // 주치의는 병동에서 정함 — 우선 요청 의사 (응급과 동일)
                    .admissionDate(requestedAt)
                    .admissionRoute(OpdAdmissionRequestedEvent.ADMISSION_ROUTE)
                    .admissionDeptId(data.targetDeptCode())
                    .status(AdmissionRequestedEvent.INITIAL_STATUS)
                    .admissionRequestId(requestId)
                    .encounterId(data.encounterId())
                    .wardPref(data.wardPrefer())
                    .requestedBy(data.requestedBy())
                    .note(data.note())
                    .preferredAdmitDate(preferredAdmitDate)
                    .build());
            log.info("admission created from opd request: requestId={} admissionId={}", requestId, created.getAdmissionId());
        } catch (BusinessException e) {
            // 예: ADMISSION_ALREADY_ACTIVE, ADMISSION_DATE_INVALID
            reject(requestId, e.getMessage());
        }
    }

    private void reject(String requestId, String reason) {
        log.info("opd admission request rejected: requestId={} reason={}", requestId, reason);
        resultPublisher.rejected(requestId, reason);
    }

    // occurredAt: "+09:00" 같은 오프셋이 있으면 병원 시간대로 바꾸고, 오프셋이 없으면 그대로 읽음. 비어 있으면 지금.
    private LocalDateTime toHospitalTime(String occurredAt) {
        if (isBlank(occurredAt)) {
            return DateRules.now();
        }
        try {
            return OffsetDateTime.parse(occurredAt).atZoneSameInstant(DateRules.HOSPITAL_ZONE).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(occurredAt);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
