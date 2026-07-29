package kr.co.seoulit.his.inpatientservice.common.response;

import lombok.Getter;

/**
 * 공통 응답 포맷 (가이드 11.3)
 */
@Getter
public class ApiResponse<T> {
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("200", "SUCCESS", data);
    }
    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
