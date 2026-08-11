package kr.co.seoulit.his.inpatientservice.nursing.dto;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSignDTO {
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
