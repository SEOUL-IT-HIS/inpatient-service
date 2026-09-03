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

    @KafkaListener(topics = "settlement.completed", groupId = "inpatient-service")
    public void onSettlementCompleted(SettlementCompletedEvent event) {
        admissionService.changeStatus(event.admissionId(), "DISCHARGED");
    }
}