package kr.co.seoulit.his.inpatientservice.bed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BedAssignmentRequest {

    @NotBlank
    @Size(max = 18)
    private String bedId;

    @NotNull
    private Long admissionId;

    @NotNull
    private LocalDateTime assignedAt;

    private LocalDateTime releasedAt;
}
