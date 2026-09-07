package kr.co.seoulit.his.inpatientservice.nursing.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NursingAssessmentHistoryDTO {
    private Long nursingAssessmentHistoryId;
    private String nursingAssessmentId;
    private String admissionId;
    private String allergyYn;
    private String allergyDetail;
    private String pastMedicalHistory;
    private String mentalStatusCd;
    private LocalDateTime assessedAt;
    private Integer assessorId;
    private String changeType;
    private LocalDateTime changedAt;
}
