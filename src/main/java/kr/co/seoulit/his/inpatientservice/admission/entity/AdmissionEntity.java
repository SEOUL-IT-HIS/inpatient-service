package kr.co.seoulit.his.inpatientservice.admission.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Admission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionEntity {

    @Id
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
