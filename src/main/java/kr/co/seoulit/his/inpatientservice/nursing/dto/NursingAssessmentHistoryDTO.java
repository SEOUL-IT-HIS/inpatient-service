package kr.co.seoulit.his.inpatientservice.nursing.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NursingAssessmentHistoryDTO {
    private Long nursingAssessmentHistoryId;
    private String nursingAssessmentId;
    private String admissionId;
    private String allergyYn;
    private String allergyDetail;
    private String pastMedicalHistory;
    private String mentalStatusCd;
    private LocalDateTime assessedAt;
    private String assessorId; // 기록자 = admin 직원 ID(empId, 간호사) — 숫자 직접 입력에서 직원 선택으로 바뀌며 문자열로 변경
    private String changeType;
    private LocalDateTime changedAt;
}
