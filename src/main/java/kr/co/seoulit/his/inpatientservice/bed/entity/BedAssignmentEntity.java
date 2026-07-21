package kr.co.seoulit.his.inpatientservice.bed.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bed_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedAssignmentEntity {

    @Id
    private String assignmentId;

    private String bedId;
    private String admissionId;
    private LocalDateTime assignedAt;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
