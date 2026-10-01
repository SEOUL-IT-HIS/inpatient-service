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
    private String recorderId; // 기록자 = admin 직원 ID(empId, 간호사) — 숫자 직접 입력에서 직원 선택으로 바뀌며 문자열로 변경
    private String changeType;
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
