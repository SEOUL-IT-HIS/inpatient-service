package kr.co.seoulit.his.inpatientservice.admission.event;

import kr.co.seoulit.his.inpatientservice.admission.service.AdmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// app.kafka.enabled=false 면 이 빈 자체가 안 만들어져서, 브로커 없을 때 재연결 시도/경고 로그가 안 남
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class SettlementCompletedListener {

    private final AdmissionService admissionService;

    // ErrorHandlingDeserializer — 수납 쪽 JSON 형식이 어긋난 메시지가 오면, 그 메시지에서 계속 멈춰 무한 재시도하지 않고
    // 에러 로그만 남기고 다음 메시지로 넘어가게 함 (응급 입원요청 · 검사 결과 리스너와 같은 설정)
    @KafkaListener(
            topics = "settlement.completed",
            groupId = "inpatient-service",
            properties = {
                    "value.deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer",
                    "spring.deserializer.value.delegate.class=org.springframework.kafka.support.serializer.JsonDeserializer",
                    "spring.json.value.default.type=kr.co.seoulit.his.inpatientservice.admission.event.SettlementCompletedEvent"
            })
    public void onSettlementCompleted(SettlementCompletedEvent event) {
        admissionService.changeStatus(event.admissionId(), "DISCHARGED");
    }
}