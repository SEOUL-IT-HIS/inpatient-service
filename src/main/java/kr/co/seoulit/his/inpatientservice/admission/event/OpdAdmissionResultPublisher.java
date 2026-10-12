package kr.co.seoulit.his.inpatientservice.admission.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 병동 → 외래 회신 발행 (inpatient.opd-admission.result.v1, key = requestId, source = 03 Ward System)
 * - 리스너(요청 거부 회신)와 병상배정 서비스(배정 회신)가 같이 씀
 * - app.kafka.enabled=false 면 아무것도 보내지 않음 (브로커 없이 로컬 실행할 때)
 */
@Component
public class OpdAdmissionResultPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String resultTopic;
    private final String source;
    private final boolean kafkaEnabled;

    public OpdAdmissionResultPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                       @Value("${inpatient.opd-admission.result.topic:inpatient.opd-admission.result.v1}") String resultTopic,
                                       @Value("${inpatient.opd-admission.source:03}") String source,
                                       @Value("${app.kafka.enabled}") boolean kafkaEnabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.resultTopic = resultTopic;
        this.source = source;
        this.kafkaEnabled = kafkaEnabled;
    }

    public void rejected(String requestId, String reason) {
        send(OpdAdmissionResultEvent.rejected(source, requestId, reason));
    }

    // 병상 배정 회신 — 트랜잭션이 "커밋된 뒤"에 보냄 (배정 저장이 롤백되면 외래에 "배정됨"이 잘못 가지 않도록.
    // 응급 회신(BedAssignmentServiceImpl.publishBedAssignedIfEmergency)과 같은 이유)
    public void bedAssignedAfterCommit(String requestId, String admissionId, String wardCode) {
        OpdAdmissionResultEvent event = OpdAdmissionResultEvent.bedAssigned(source, requestId, admissionId, wardCode);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    private void send(OpdAdmissionResultEvent event) {
        if (!kafkaEnabled) {
            return;
        }
        kafkaTemplate.send(resultTopic, event.data().requestId(), event);
    }
}
