package kr.co.seoulit.his.inpatientservice.prescription.event;

import kr.co.seoulit.his.inpatientservice.prescription.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 검사서비스 → 검사 결과 수신 (lab.lab-result.reported.v1)
 * - 병동이 등록한 입원 처방의 결과만 처방 항목에 저장 (외래 처방 결과는 서비스에서 무시)
 * - 외래도 같은 토픽을 구독하지만 groupId가 달라서(inpatient-service) 서로 영향 없이 각자 전부 받음
 * - 토픽 이름은 기본값을 둬서, 설정에 줄이 없는 환경(Release 등)에서도 서버가 뜨도록 함
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class LabResultReportedListener {

    private final PrescriptionService prescriptionService;

    // properties 설명은 AdmissionRequestedListener와 동일:
    // 전역 기본 타입(SettlementCompletedEvent) 대신 이 이벤트 타입으로 읽고, 형식이 깨진 메시지는 건너뜀
    @KafkaListener(
            topics = "${lab.result.topic:lab.lab-result.reported.v1}",
            groupId = "inpatient-service",
            properties = {
                    "value.deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer",
                    "spring.deserializer.value.delegate.class=org.springframework.kafka.support.serializer.JsonDeserializer",
                    "spring.json.value.default.type=kr.co.seoulit.his.inpatientservice.prescription.event.LabResultReportedEvent"
            })
    public void onLabResultReported(LabResultReportedEvent event) {
        prescriptionService.applyLabResult(event);
    }
}
