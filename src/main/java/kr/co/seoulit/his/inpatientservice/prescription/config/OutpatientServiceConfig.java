package kr.co.seoulit.his.inpatientservice.prescription.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OutpatientServiceConfig {
    @Bean
    public RestClient outpatientRestClient(
            @Value("${app.outpatient-service.host}") String host,
            @Value("${app.outpatient-service.port}") String port) {
        String baseUrl = "http://" + host + ":" + port;
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
