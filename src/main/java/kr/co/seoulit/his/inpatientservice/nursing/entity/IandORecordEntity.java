package kr.co.seoulit.his.inpatientservice.nursing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "intake_output_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IandORecordEntity {
    @Id
    private String intakeOutputId;
    private String admissionId;
    private LocalDateTime recordedAt;
    private String ioTypeCd;
    private String routeCd;
    private Integer amountMl;
    private Integer recorderId;
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
