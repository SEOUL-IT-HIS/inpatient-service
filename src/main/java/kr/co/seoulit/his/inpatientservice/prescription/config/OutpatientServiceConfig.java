package kr.co.seoulit.his.inpatientservice.prescription.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class OutpatientServiceConfig {
    @Bean
    public RestClient outpatientRestClient(
            @Value("${app.outpatient-service.host}") String host,
            @Value("${app.outpatient-service.port}") String port) {
        String baseUrl = "http://" + host + ":" + port;

        // JDK HttpClient 기반으로 보냄 — 예전 SimpleClientHttpRequestFactory(HttpURLConnection)는 PATCH를 지원하지 않아서
        // 처방 취소(PATCH deactivate)가 요청이 나가기도 전에 예외가 났고, 화면에는 "Outpatient service is unavailable"만 보였음
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)         // 평문(http)에서 HTTP/2 업그레이드를 시도하지 않게
                .connectTimeout(Duration.ofSeconds(3))        // 연결까지 최대 3초
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));       // 응답까지 최대 10초

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

}
