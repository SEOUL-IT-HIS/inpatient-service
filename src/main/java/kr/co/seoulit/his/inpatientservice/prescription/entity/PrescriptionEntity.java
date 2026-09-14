package kr.co.seoulit.his.inpatientservice.prescription.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionEntity {
    @Id
    private String prescriptionId;
    private String encounterId;
    private String patientId;
    private String admissionId;
    private String serviceType;
    private String status;
    private LocalDateTime prescribedAt;
    private String prescribedBy;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private String orderMethod;
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
    private String pharmacySendStatus;
    private LocalDateTime pharmacySentAt;
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
