package kr.co.seoulit.his.inpatientservice.common.exception;

import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e){
        HttpStatus status = e.getErrorCode().getStatus();
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(String.valueOf(status.value()), e.getMessage()));

    }
}
