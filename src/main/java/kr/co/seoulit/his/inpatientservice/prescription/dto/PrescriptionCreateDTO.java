package kr.co.seoulit.his.inpatientservice.prescription.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionCreateDTO {
    private String patientId; // 프론트에서는 비워서 보내면 됨 - admissionId로 서버가 채워서 외래로 전달
    private String serviceType;
    private String orderMethod;
    private String priorityCode;
    private String timingCode;
    private List<PrescriptionItemDTO> items;
}
