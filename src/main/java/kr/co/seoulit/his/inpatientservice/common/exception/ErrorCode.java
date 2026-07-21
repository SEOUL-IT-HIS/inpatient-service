package kr.co.seoulit.his.inpatientservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    BED_ASSIGNMENT_NOT_FOUND("Bed assignment not found"),
    BED_NOT_AVAILABLE("Bed is not available");

    private final String message;
}
