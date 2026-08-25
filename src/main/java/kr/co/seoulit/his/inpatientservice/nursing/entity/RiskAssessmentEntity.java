package kr.co.seoulit.his.inpatientservice.nursing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessmentEntity {
    @Id
    private String patientRiskAssessmentId;
    private String admissionId;
    private String assessmentTypeCd;
    private int score;
    private String riskLevelCd;
    private LocalDateTime assessedAt;
    private int assessorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
