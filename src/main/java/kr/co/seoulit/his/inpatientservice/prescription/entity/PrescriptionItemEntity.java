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
