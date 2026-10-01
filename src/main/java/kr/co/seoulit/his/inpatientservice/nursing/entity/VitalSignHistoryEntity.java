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
    private String recorderId; // 기록자 = admin 직원 ID(empId, 간호사) — 숫자 직접 입력에서 직원 선택으로 바뀌며 문자열로 변경
    private String changeType;

    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
