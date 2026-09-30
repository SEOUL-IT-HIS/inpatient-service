package kr.co.seoulit.his.inpatientservice.common.exception;

import lombok.Getter;

@Getter

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // ErrorCode의 고정 메시지 대신 상세 사유를 담고 싶을 때 (예: 외래 서비스가 돌려준 에러 메시지)
    // HTTP 상태는 그대로 errorCode.getStatus()를 따름
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
