package kr.co.seoulit.his.inpatientservice.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * 응급 입원요청 연동 토픽 생성
 * - 서버가 뜰 때 브로커에 토픽이 없으면 만들어 줌 (이미 있으면 그대로 둠)
 * - 공용 브로커가 토픽 자동 생성을 막아둔 경우에도 동작하도록 명시적으로 등록
 * - requested 토픽은 응급이 발행하는 토픽이지만, 병동이 먼저 켜져도 구독할 수 있도록 같이 등록
 * - app.kafka.enabled=false면 등록하지 않음 (브로커 없이 로컬 실행할 때)
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaTopicConfig {

    @Bean
    public NewTopic admissionRequestedTopic(@Value("${emergency.admission.requested.topic}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic bedAssignedTopic(@Value("${inpatient.admission.bed-assigned.topic}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic admissionRejectedTopic(@Value("${inpatient.admission.rejected.topic}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }
}
