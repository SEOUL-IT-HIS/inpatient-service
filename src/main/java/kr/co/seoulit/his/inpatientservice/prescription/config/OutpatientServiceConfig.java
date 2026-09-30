package kr.co.seoulit.his.inpatientservice.prescription.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class OutpatientServiceConfig {
    @Bean
    public RestClient outpatientRestClient(
            @Value("${app.outpatient-service.host}") String host,
            @Value("${app.outpatient-service.port}") String port) {
        String baseUrl = "http://" + host + ":" + port;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));  // 연결까지 최대 3초
        factory.setReadTimeout(Duration.ofSeconds(10));    // 응답까지 최대 10초

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

}
