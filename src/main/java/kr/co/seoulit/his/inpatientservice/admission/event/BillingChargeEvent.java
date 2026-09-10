package kr.co.seoulit.his.inpatientservice.admission.event;

/**
 * billing-service의 inpatient-billing-charge 토픽 컨슈머(BillingChargeKafkaConsumer) 컨트랙트에 맞춘 이벤트.
 * amount는 billing 쪽에서 billing_master 단가 × quantity로 재계산하므로 보내지 않는다.
 * feeCode="DISCHARGE_REQUEST"는 billing_master 등록 없이 소비 즉시 무시되는 특수값(수납팀 합의 완료).
 */
public record BillingChargeEvent(
        String patientId,
        String receptionId,
        String admissionId,
        String sourceServiceCode,
        String sourceRecordId,
        String feeCode,
        String itemName,
        String quantity
) {
    public static final String SOURCE_SERVICE_CODE = "5f25f504-81e4-4343-bba0-6441c4a332e9";
    public static final String DISCHARGE_REQUEST_FEE_CODE = "DISCHARGE_REQUEST";

    public static BillingChargeEvent dischargeRequest(String patientId, String admissionId) {
        return new BillingChargeEvent(
                patientId,
                null,
                admissionId,
                SOURCE_SERVICE_CODE,
                admissionId,
                DISCHARGE_REQUEST_FEE_CODE,
                "퇴원신청",
                "0"
        );
    }

    public static BillingChargeEvent roomFee(String patientId, String admissionId, String feeCode, long days) {
        return new BillingChargeEvent(
                patientId,
                null,
                admissionId,
                SOURCE_SERVICE_CODE,
                admissionId,
                feeCode,
                "입원료",
                String.valueOf(days)
        );
    }
}
