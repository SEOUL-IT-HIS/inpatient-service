package kr.co.seoulit.his.inpatientservice.nursing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "restraint")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestraintEntity {
    @Id
    private String restraintId;
    private String admissionId;
    private String restraintTypeCd;
    private LocalDateTime appliedAt;
    private String reason;
    private String doctorOrderId;
    private String evaluatorId;
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
