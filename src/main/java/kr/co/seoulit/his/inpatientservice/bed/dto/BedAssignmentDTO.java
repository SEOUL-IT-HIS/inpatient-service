package kr.co.seoulit.his.inpatientservice.bed.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedAssignmentDTO {
    private String assignmentId;
    private String bedId;
    private String admissionId;
    private LocalDateTime assignedAt;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
