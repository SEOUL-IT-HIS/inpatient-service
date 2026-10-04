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
    ADMISSION_ALREADY_DISCHARGED("Admission is already discharged", HttpStatus.CONFLICT),
    DOCTOR_ID_REQUIRED("Doctor ID is required", HttpStatus.BAD_REQUEST),
    ATTENDING_DOCTOR_NOT_ASSIGNED("Attending doctor is not assigned to this admission. Assign a doctor in Admission Details first", HttpStatus.BAD_REQUEST),
    ROOM_TYPE_FEE_CODE_NOT_MAPPED("No billing fee code mapped for this room type", HttpStatus.CONFLICT),
    PRESCRIPTION_NOT_FOUND("Prescription not found", HttpStatus.NOT_FOUND),
    PRESCRIPTION_ALREADY_CANCELLED("Prescription is already cancelled", HttpStatus.CONFLICT),
    // 외래(처방코어) 연동 실패 — PrescriptionCoreClient에서 사용
    OUTPATIENT_PRESCRIPTION_REJECTED("Outpatient service rejected the prescription request", HttpStatus.BAD_REQUEST), // 외래가 4xx 응답
    OUTPATIENT_SERVICE_ERROR("Outpatient service error", HttpStatus.BAD_GATEWAY), // 외래가 5xx 응답 or 응답이 비어 있음
    OUTPATIENT_SERVICE_UNAVAILABLE("Outpatient service is unavailable", HttpStatus.SERVICE_UNAVAILABLE), // 외래 서버 연결 실패/타임아웃
    DISCHARGE_ALREADY_REQUESTED("Discharge already requested for this admission", HttpStatus.CONFLICT),
    ADMISSION_ALREADY_HAS_BED("This admission already has an active bed assignment", HttpStatus.CONFLICT),
    BED_ASSIGNED_AT_INVALID("Assigned date must be today or later", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

}
