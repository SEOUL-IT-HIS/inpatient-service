package kr.co.seoulit.his.inpatientservice.prescription.client;

import com.sun.net.httpserver.HttpServer;
import kr.co.seoulit.his.inpatientservice.prescription.config.OutpatientServiceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

// 외래 처방 취소(PATCH)가 실제로 외래까지 도달하는지 — 가짜 외래 서버(JDK HttpServer)를 띄워 확인
// (예전에는 외래용 RestClient가 HttpURLConnection 기반이라 PATCH를 보낼 수 없어서,
//  요청이 나가기도 전에 예외가 나고 화면에는 "Outpatient service is unavailable"만 보였음)
class PrescriptionCoreClientCancelTest {

    private HttpServer server;
    private final AtomicReference<String> receivedMethod = new AtomicReference<>();
    private final AtomicReference<String> receivedQuery = new AtomicReference<>();

    @BeforeEach
    void startFakeOutpatient() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/outpatient/prescriptions/P1/deactivate", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedQuery.set(exchange.getRequestURI().getQuery()); // 디코딩된 쿼리
            byte[] body = "{\"code\":\"SUCCESS\",\"message\":\"ok\",\"data\":\"cancelled\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopFakeOutpatient() {
        server.stop(0);
    }

    @Test
    void 처방_취소는_PATCH로_외래에_도달하고_한글_사유도_그대로_전달된다() {
        RestClient restClient = new OutpatientServiceConfig()
                .outpatientRestClient("localhost", String.valueOf(server.getAddress().getPort()));
        PrescriptionCoreClient client = new PrescriptionCoreClient(restClient);

        client.deactivate("P1", "검사 오더 중복 test", "EMP1");

        assertThat(receivedMethod.get()).isEqualTo("PATCH");
        assertThat(receivedQuery.get()).contains("cancelReason=검사 오더 중복 test").contains("userId=EMP1");
    }
}
