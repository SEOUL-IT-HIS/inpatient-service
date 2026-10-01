package kr.co.seoulit.his.inpatientservice.prescription.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItemDTO {
    private String itemId;
    private String prescriptionId;
    private String prescriptionType;
    private String itemCode;
    private String itemName;
    private Double dosage;
    private String frequency;
    private String durationDays;
    private String detailInfo;
    private String sendStatus;
    private LocalDateTime sentAt;
    private String labOrderId;
    private String rejectReason;
    private String dosageFormCd;

    // 검사 결과 (lab.lab-result.reported.v1 수신 시 병동이 채움 — 외래 응답에는 없음)
    private String resultStatus;            // 예: FINAL
    private String resultSummary;           // 결과값 요약 (예: "02: 6.2 x10^3/uL (4.0-10.0)")
    private LocalDateTime resultReportedAt;
}
