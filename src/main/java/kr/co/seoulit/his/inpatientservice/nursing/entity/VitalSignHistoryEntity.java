package kr.co.seoulit.his.inpatientservice.nursing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vital_sign_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSignHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vital_sign_history_seq")
    @SequenceGenerator(name = "vital_sign_history_seq", sequenceName = "vital_sign_history_seq", allocationSize = 1)
    private Long vitalSignHistoryId;

    private String vitalSignId;
    private String admissionId;
    private LocalDateTime measuredAt;
    private int temperature;
    private int pulse;
    private int respiration;
    private int bpSystolic;
    private int bpDiastolic;
    private int spo2;
    private int recorderId;

    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
