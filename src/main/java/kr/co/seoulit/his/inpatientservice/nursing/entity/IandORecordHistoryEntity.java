package kr.co.seoulit.his.inpatientservice.nursing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "intake_output_record_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IandORecordHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "intakeOutput_history_seq")
    @SequenceGenerator(name = "intakeOutput_history_seq", sequenceName = "intakeOutput_history_seq", allocationSize = 1)
    private Long intakeOutputHistoryId;

    private String intakeOutputId;
    private String admissionId;
    private LocalDateTime recordedAt;
    private String ioTypeCd;
    private String routeCd;
    private Integer amountMl;
    private Integer recorderId;
    private String changeType;
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
