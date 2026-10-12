package kr.co.seoulit.his.inpatientservice.admission.event;

import java.time.OffsetDateTime;
import java.util.UUID;

import kr.co.seoulit.his.inpatientservice.common.util.DateRules;

/**
 * 병동 → 외래 회신 (토픽: inpatient.opd-admission.result.v1, key: requestId)
 * 종류는 eventType으로 구분:
 *  - AdmissionBedAssigned  병상 배정됨   (status 02, admissionId · assignedWardCode 채움)
 *  - AdmissionRejected     입원요청 거부 (status 03, rejectReason 채움)
 * (외래와 합의: 취소·수정은 이번 연동 범위에서 제외)
 */
public record OpdAdmissionResultEvent(
        String eventId,
        String eventType,
        String version,
        String occurredAt,
        String source,
        Data data
) {
    public static final String STATUS_BED_ASSIGNED = "02"; // ADMISSION_REQUEST_STATUS_CD
    public static final String STATUS_REJECTED = "03";

    public record Data(
            String requestId,
            String status,
            String admissionId,
            String assignedWardCode,
            String rejectReason
    ) {}

    public static OpdAdmissionResultEvent bedAssigned(String source, String requestId, String admissionId,
                                                      String wardCode) {
        return of(source, "AdmissionBedAssigned",
                new Data(requestId, STATUS_BED_ASSIGNED, admissionId, wardCode, null));
    }

    public static OpdAdmissionResultEvent rejected(String source, String requestId, String reason) {
        return of(source, "AdmissionRejected", new Data(requestId, STATUS_REJECTED, null, null, reason));
    }

    private static OpdAdmissionResultEvent of(String source, String eventType, Data data) {
        return new OpdAdmissionResultEvent(UUID.randomUUID().toString(), eventType, "1",
                OffsetDateTime.now(DateRules.HOSPITAL_ZONE).toString(), source, data);
    }
}
