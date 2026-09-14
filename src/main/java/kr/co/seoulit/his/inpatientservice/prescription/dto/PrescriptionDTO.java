package kr.co.seoulit.his.inpatientservice.prescription.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDTO {
    private String prescriptionId;
    private String encounterId;
    private String patientId;
    private String patientName;
    private String serviceType;
    private String status;
    private LocalDateTime prescribedAt;
    private String prescribedBy;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private String orderMethod;
    private String admissionId;

    private String priorityCode;
    private String timingCode;
    private String verbalYn;
    private LocalDateTime verbalConfirmedAt;
    private String verbalConfirmedBy;
    private String recorderId;
    private String holdReason;
    private String holdBy;
    private LocalDateTime holdAt;
    private String discontinuedReason;
    private String discontinuedBy;
    private LocalDateTime discontinuedAt;

    private List<PrescriptionItemDTO> items;

    private String pharmacySendStatus;
    private LocalDateTime pharmacySentAt;
}
