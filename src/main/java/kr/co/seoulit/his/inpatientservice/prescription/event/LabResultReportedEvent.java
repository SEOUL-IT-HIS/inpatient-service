package kr.co.seoulit.his.inpatientservice.prescription.event;

import java.util.List;

/**
 * 검사서비스 → 검사 결과 보고 (토픽: lab.lab-result.reported.v1)
 * 외래가 공유한 페이로드 예시 그대로. 매칭 키는 data.prescriptionId + items[].itemCode (외래 권장)
 * 시각 필드는 "+09:00" 오프셋이 붙은 문자열이라 String으로 받고, 저장할 때 변환
 */
public record LabResultReportedEvent(
        String eventId,
        String eventType,       // "LabResultReported"
        String version,
        String occurredAt,
        String source,          // "LAB"
        String correlationId,
        Data data
) {
    public record Data(
            String prescriptionId,
            String labOrderId,
            String resultStatus, // 예: FINAL
            String reportedAt,
            List<Item> items
    ) {}

    public record Item(
            String itemCode,     // 예: CBC
            String resultType,
            String resultId,
            List<Detail> details
    ) {}

    public record Detail(
            Integer seq,
            String detailCode,
            String resultValue,
            String unit,
            String referenceRange,
            String abnormalFlag  // N(정상) / H, L 등(이상)
    ) {}
}
