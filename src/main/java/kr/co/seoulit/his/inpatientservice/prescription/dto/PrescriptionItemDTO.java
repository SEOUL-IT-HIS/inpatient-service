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
}
