package kr.co.seoulit.his.inpatientservice.nursing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessmentHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "risk_assessment_history_seq")
    @SequenceGenerator(name = "risk_assessment_history_seq", sequenceName = "risk_assessment_history_seq", allocationSize = 1)
    private Long patientRiskAssessmentHistoryId;

    private String patientRiskAssessmentId;
    private String admissionId;
    private String assessmentTypeCd;
    private int score;
    private String riskLevelCd;
    private LocalDateTime assessedAt;
    private int assessorId;
    private String changeType;
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
