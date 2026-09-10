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
    BED_NOT_FOUND("Bed not found", HttpStatus.NOT_FOUND),
    BED_RESERVATION_NOT_FOUND("Bed reservation not found", HttpStatus.NOT_FOUND),
    BED_RESERVATION_ALREADY_ACTIVE("Bed already has an active reservation", HttpStatus.CONFLICT),
    ADMISSION_NOT_FOUND("Admission not found", HttpStatus.NOT_FOUND),
    ADMISSION_ALREADY_ACTIVE("Patient already has an active admission",HttpStatus.CONFLICT),
    ROOM_TYPE_FEE_CODE_NOT_MAPPED("No billing fee code mapped for this room type", HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus status;

}
