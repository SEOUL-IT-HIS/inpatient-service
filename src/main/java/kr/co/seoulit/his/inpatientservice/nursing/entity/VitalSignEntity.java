package kr.co.seoulit.his.inpatientservice.nursing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "vital_sign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSignEntity {
    @Id
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
