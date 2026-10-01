package kr.co.seoulit.his.inpatientservice.prescription.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescription_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItemEntity {
    @Id
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

    // 검사 결과 — 검사서비스가 발행한 lab.lab-result.reported.v1을 병동이 받아서 prescriptionId + itemCode로 매칭해 저장
    private String resultStatus;
    @jakarta.persistence.Column(length = 1000)
    private String resultSummary;
    private LocalDateTime resultReportedAt;

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
