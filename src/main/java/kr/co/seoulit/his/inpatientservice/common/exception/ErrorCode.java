package kr.co.seoulit.his.inpatientservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    BED_ASSIGNMENT_NOT_FOUND("Bed assignment not found", HttpStatus.NOT_FOUND),
    BED_NOT_AVAILABLE("Bed is not available", HttpStatus.CONFLICT),
    BED_ALREADY_OCCUPIED("Bed is already occupied", HttpStatus.CONFLICT),
    BED_NOT_FOUND("Bed not found", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus status;

}
