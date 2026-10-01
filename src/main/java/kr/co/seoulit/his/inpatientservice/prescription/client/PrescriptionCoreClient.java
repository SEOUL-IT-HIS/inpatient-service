package kr.co.seoulit.his.inpatientservice.prescription.client;

import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.prescription.dto.OutpatientApiResponse;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionCreateDTO;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Supplier;

/**
 * 외래 처방코어 호출
 * - 등록: POST /api/outpatient/prescriptions/admission/{admissionId}
 * - 전송: POST /api/outpatient/prescriptions/{prescriptionId}/dispatch-lab | dispatch-pharmacy (자동 전송 아님 — 등록 후 따로 호출)
 * - 취소: PATCH /api/outpatient/prescriptions/{prescriptionId}/deactivate?cancelReason=...&userId=... (body 아님, 쿼리 파라미터)
 * 실패는 모두 call()에서 우리 BusinessException(400/502/503)으로 바꿔서 던짐
 */
@Component
@RequiredArgsConstructor
public class PrescriptionCoreClient {

    private static final ParameterizedTypeReference<OutpatientApiResponse<PrescriptionDTO>> PRESCRIPTION_RESPONSE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<OutpatientApiResponse<Object>> ANY_RESPONSE =
            new ParameterizedTypeReference<>() {};

    private final RestClient outpatientRestClient;

    public PrescriptionDTO createPrescription(String admissionId, PrescriptionCreateDTO requestDto) {
        OutpatientApiResponse<PrescriptionDTO> response = call(() -> outpatientRestClient.post()
                .uri("/api/outpatient/prescriptions/admission/{admissionId}", admissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve() // 외래가 4xx/5xx를 주면 여기서 RestClientResponseException이 던져짐
                .body(PRESCRIPTION_RESPONSE));

        // 200인데 body나 data가 비어 있으면, 서비스에서 created.setAdmissionId(...) 할 때 NPE가 나므로 여기서 막음
        if (response == null || response.data() == null) {
            throw new BusinessException(ErrorCode.OUTPATIENT_SERVICE_ERROR,
                    ErrorCode.OUTPATIENT_SERVICE_ERROR.getMessage() + ": empty response");
        }
        return response.data();
    }

    // 검사실로 전송 (처방 중 prescriptionType "검사" 항목)
    public void dispatchLab(String prescriptionId) {
        call(() -> outpatientRestClient.post()
                .uri("/api/outpatient/prescriptions/{prescriptionId}/dispatch-lab", prescriptionId)
                .retrieve()
                .body(ANY_RESPONSE));
    }

    // 약제부로 전송 (처방 중 prescriptionType "약품" 항목)
    public void dispatchPharmacy(String prescriptionId) {
        call(() -> outpatientRestClient.post()
                .uri("/api/outpatient/prescriptions/{prescriptionId}/dispatch-pharmacy", prescriptionId)
                .retrieve()
                .body(ANY_RESPONSE));
    }

    // 처방 취소 — 외래가 body가 아니라 쿼리 파라미터로 받음
    public void deactivate(String prescriptionId, String cancelReason, String userId) {
        call(() -> outpatientRestClient.patch()
                .uri(uri -> uri.path("/api/outpatient/prescriptions/{prescriptionId}/deactivate")
                        .queryParam("cancelReason", cancelReason)
                        .queryParam("userId", userId)
                        .build(prescriptionId))
                .retrieve()
                .body(ANY_RESPONSE));
    }

    // 외래 호출 공통 에러 처리 — 실패 종류별로 우리 예외로 바꿈
    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientResponseException e) {
            // 외래가 응답은 했지만 실패(4xx/5xx) — 외래 에러 body의 사유를 담아서 우리 예외로 바꿈
            throw toBusinessException(e);
        } catch (ResourceAccessException e) {
            // 외래 서버에 아예 연결이 안 됨 (서버 꺼짐, 포트 틀림, 타임아웃 등)
            throw new BusinessException(ErrorCode.OUTPATIENT_SERVICE_UNAVAILABLE);
        } catch (RestClientException e) {
            // 그 외 — 응답은 200인데 JSON 형식이 달라서 변환 실패 등
            throw new BusinessException(ErrorCode.OUTPATIENT_SERVICE_ERROR,
                    ErrorCode.OUTPATIENT_SERVICE_ERROR.getMessage() + ": " + e.getMessage());
        }
    }

    // 외래 실패 응답 → 우리 BusinessException
    // 외래 에러 포맷: { "code": "OPD004", "message": "...", "data": null }
    // - 4xx: 요청이 잘못됨(입력값, 없는 입원 건 등) → OUTPATIENT_PRESCRIPTION_REJECTED(400)
    // - 5xx: 외래 내부 오류 → OUTPATIENT_SERVICE_ERROR(502)
    // 화면에는 "우리 메시지: 외래 메시지 (외래 코드)"가 그대로 보임 (프론트 axios가 응답의 message를 에러 문구로 씀)
    private BusinessException toBusinessException(RestClientResponseException e) {
        ErrorCode errorCode = e.getStatusCode().is4xxClientError()
                ? ErrorCode.OUTPATIENT_PRESCRIPTION_REJECTED
                : ErrorCode.OUTPATIENT_SERVICE_ERROR;

        String detail = "HTTP " + e.getStatusCode().value(); // 외래 에러 body를 못 읽으면 상태코드라도 남김
        try {
            OutpatientApiResponse<?> body = e.getResponseBodyAs(OutpatientApiResponse.class);
            if (body != null && body.message() != null) {
                detail = body.message() + " (" + body.code() + ")";
            }
        } catch (RuntimeException ignored) {
            // 외래 공통 포맷이 아닌 응답(예: 엔드포인트가 아직 없어서 나는 Spring 기본 404 페이지) — detail은 상태코드로 둠
        }
        return new BusinessException(errorCode, errorCode.getMessage() + ": " + detail);
    }
}
