package kr.co.seoulit.his.inpatientservice.bed.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BedAssignmentResponse {

    private Long assignmentId;
    private String bedId;
    private Long admissionId;
    private LocalDateTime assignedAt;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
