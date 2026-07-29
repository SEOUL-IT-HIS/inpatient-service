package kr.co.seoulit.his.inpatientservice.admission.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionDTO {
    private String admissionId;
    private String patientId;
    private String doctorId;
    private LocalDateTime admissionDate;
    private String admissionRoute;
    private String admissionDeptId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
