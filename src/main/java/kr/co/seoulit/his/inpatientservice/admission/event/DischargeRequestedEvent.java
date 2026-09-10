package kr.co.seoulit.his.inpatientservice.admission.event;

/**
 * billing-service의 수납정보등록(BillingChargeRequestDTO)과 동일한 형태로 통일한 이벤트.
 * 실제 청구 항목이 아니라 "이 admission엔 더 이상 청구가 없다"는 신호이므로
 * feeCode="DISCHARGE_REQUEST"로 표시하고 amount/quantity는 0으로 보낸다.
 * billing-service 컨슈머는 feeCode가 DISCHARGE_REQUEST면 청구 항목으로 적립하지 않고
 * "정산 대상"으로만 노출해야 한다 (수납 담당자와 합의 필요).
 */
public record DischargeRequestedEvent(
        String patientId,
        String receptionId,
        String admissionId,
        String sourceServiceCode,
        String sourceRecordId,
        String feeCode,
        String itemName,
        String quantity,
        String amount
) {
    public static final String SOURCE_SERVICE_CODE = "INPATIENT";
    public static final String FEE_CODE = "DISCHARGE_REQUEST";

    public static DischargeRequestedEvent of(String patientId, String admissionId) {
        return new DischargeRequestedEvent(
                patientId,
                "",
                admissionId,
                SOURCE_SERVICE_CODE,
                admissionId,
                FEE_CODE,
                "퇴원신청",
                "0",
                "0"
        );
    }

    public static DischargeRequestedEvent roomFee(String patientId, String admissionId, String roomTypeCode, long days) {
        return new DischargeRequestedEvent(
                patientId,
                "",
                admissionId,
                SOURCE_SERVICE_CODE,
                admissionId,
                roomTypeCode,
                "입원료",
                String.valueOf(days),
                "0"
        );
    }


}
