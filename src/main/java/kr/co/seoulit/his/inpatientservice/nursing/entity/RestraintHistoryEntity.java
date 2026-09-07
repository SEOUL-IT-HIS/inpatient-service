package kr.co.seoulit.his.inpatientservice.nursing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "restraint_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestraintHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restraint_history_seq")
    @SequenceGenerator(name = "restraint_history_seq", sequenceName = "restraint_history_seq", allocationSize = 1)
    private Long restraintHistoryId;

    private String restraintId;
    private String admissionId;
    private String restraintTypeCd;
    private LocalDateTime appliedAt;
    private String reason;
    private String doctorOrderId;
    private String evaluatorId;
    private String changeType;
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
