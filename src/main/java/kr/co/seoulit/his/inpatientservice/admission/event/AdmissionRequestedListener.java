package kr.co.seoulit.his.inpatientservice.admission.event;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.admission.service.AdmissionService;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 응급 → 병동 입원요청 수신 (emergency.admission.requested.v1)
 * - 받은 요청으로 입원 건(REQUESTED, 경로 Emergency)을 만들면 입퇴원관리 > Admission Requests 목록에 바로 나타남
 * - 입원 건을 만들 수 없으면(예: 이미 입원 중인 환자) 그 사유로 ADMISSION_REJECTED를 응급에 자동 회신
 *   (병동 직원이 직접 거절하는 기능은 없음)
 * - app.kafka.enabled=false 면 이 빈 자체가 안 만들어짐 (SettlementCompletedListener와 동일)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class AdmissionRequestedListener {

    private final AdmissionService admissionService;
    private final AdmissionRepository admissionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String rejectedTopic;

    public AdmissionRequestedListener(AdmissionService admissionService,
                                      AdmissionRepository admissionRepository,
                                      KafkaTemplate<String, Object> kafkaTemplate,
                                      @Value("${inpatient.admission.rejected.topic}") String rejectedTopic) {
        this.admissionService = admissionService;
        this.admissionRepository = admissionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.rejectedTopic = rejectedTopic;
    }

    // properties: 이 리스너에만 적용되는 consumer 설정
    // 1) value.default.type — 전역 설정은 모든 메시지를 SettlementCompletedEvent로 읽게 되어 있어서, 이 리스너는 자기 타입으로 덮어씀
    // 2) ErrorHandlingDeserializer — 응급 쪽 JSON 형식이 어긋난 메시지가 오면, 그 메시지에서 계속 멈춰 무한 재시도하지 않고
    //    에러 로그만 남기고 다음 메시지로 넘어가게 함 (연동 테스트 중 형식 불일치 대비)
    @KafkaListener(
            topics = "${emergency.admission.requested.topic}",
            groupId = "inpatient-service",
            properties = {
                    "value.deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer",
                    "spring.deserializer.value.delegate.class=org.springframework.kafka.support.serializer.JsonDeserializer",
                    "spring.json.value.default.type=kr.co.seoulit.his.inpatientservice.admission.event.AdmissionRequestedEvent"
            })
    public void onAdmissionRequested(AdmissionRequestedEvent event) {
        log.info("admission request received: dispositionId={} patientId={}", event.dispositionId(), event.patientId());

        // 회신할 키가 없으면 처리할 수 없음 — 로그만 남기고 버림
        if (event.dispositionId() == null || event.dispositionId().isBlank()) {
            log.warn("admission request without dispositionId ignored: {}", event);
            return;
        }

        // 같은 요청이 다시 오면(응급 재발행 등) 입원 건을 두 번 만들지 않도록 무시
        if (admissionRepository.existsByDispositionId(event.dispositionId())) {
            log.info("duplicate admission request ignored: dispositionId={}", event.dispositionId());
            return;
        }

        // 환자 ID가 없으면 입원 건을 만들 수 없으므로 바로 거절 회신
        if (event.patientId() == null || event.patientId().isBlank()) {
            reject(event.dispositionId(), "patientId is required");
            return;
        }

        try {
            // 입원 건 생성은 기존 로직 그대로 재사용 — 진행중 입원 중복 확인, 입원 ID(A001...) 생성 포함
            AdmissionDTO created = admissionService.createAdmission(event.toAdmissionDto());
            log.info("admission created from emergency request: dispositionId={} admissionId={}",
                    event.dispositionId(), created.getAdmissionId());
        } catch (BusinessException e) {
            // 예: ADMISSION_ALREADY_ACTIVE("Patient already has an active admission")
            reject(event.dispositionId(), e.getMessage());
        }
    }

    // 병동 → 응급 거절 회신 (key = dispositionId, 응급팀 합의)
    private void reject(String dispositionId, String reason) {
        log.info("admission request rejected: dispositionId={} reason={}", dispositionId, reason);
        kafkaTemplate.send(rejectedTopic, dispositionId, new AdmissionRejectedEvent(dispositionId, reason));
    }
}
