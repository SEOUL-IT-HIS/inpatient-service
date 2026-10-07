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
    // 퇴원신청 시각 — DISCHARGE_REQUESTED가 될 때 기록 (신청 전이면 null)
    // 입원료는 이 날짜까지의 일수로 청구됨 (퇴원 확정 dischargedAt은 정산이 끝난 시각이라 더 늦을 수 있음)
    private LocalDateTime dischargeRequestedAt;
    private LocalDateTime dischargedAt;   // 퇴원 확정 시각 — 정산 완료로 DISCHARGED가 될 때 기록 (퇴원 전이면 null)
    private String admissionRoute;
    private String admissionDeptId;
    private String status;

    // 응급 입원요청으로 들어온 건만 채워짐 (AdmissionEntity 주석 참고)
    private String dispositionId;
    private String admissionRequestId;
    private String encounterId;
    private String wardPref;
    private String isolationYn;
    private String requestedBy;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
