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
    private String prescribedBy; // 처방의사 ID - 외래가 서버 간 호출이라 세션으로 못 채움. 프론트는 비워서 보내고, 서버가 입원 건의 주치의(admission.doctorId)로 채워서 외래로 전달
    private String serviceType;
    private String orderMethod;
    private String priorityCode;
    private String timingCode;
    private List<PrescriptionItemDTO> items;
}
