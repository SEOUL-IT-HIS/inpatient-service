package kr.co.seoulit.his.inpatientservice.nursing.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestraintHistoryDTO {
    private Long restraintHistoryId;
    private String restraintId;
    private String admissionId;
    private String restraintTypeCd;
    private LocalDateTime appliedAt;
    private String reason;
    private String doctorOrderId;
    private String evaluatorId;
    private String changeType;
    private LocalDateTime changedAt;
}
