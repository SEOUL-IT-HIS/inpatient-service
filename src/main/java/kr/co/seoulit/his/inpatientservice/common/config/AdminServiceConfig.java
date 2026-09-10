package kr.co.seoulit.his.inpatientservice.common.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AdminServiceConfig {
    @Bean
    public RestClient adminRestClient(
            @Value("${app.admin-service.host}") String host,
            @Value("${app.admin-service.port}") String port) {
        String baseUrl = "http://" + host + ":" + port;
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
