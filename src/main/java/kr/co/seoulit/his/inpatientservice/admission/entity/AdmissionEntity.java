package kr.co.seoulit.his.inpatientservice.admission.entity;

import jakarta.persistence.Column;
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
    private LocalDateTime dischargedAt;   // 퇴원 확정 시각 — 정산 완료로 DISCHARGED가 될 때 기록 (퇴원 전이면 null)
    private String admissionRoute;
    private String admissionDeptId;
    private String status;

    // ---- 응급 입원요청(Kafka emergency.admission.requested.v1)으로 들어온 건만 채워짐 ----
    // 외래/병동 직접 등록 건은 모두 null
    @Column(unique = true)
    private String dispositionId;   // 응급 요청 식별자 — 회신(BED_ASSIGNED/REJECTED)에 그대로 돌려줌, 중복 수신 확인용
    private String admissionRequestId; // 이 입원 건을 만든 응급 요청의 ID — 나중에 병상 배정 회신에 그대로 넣기 위해 저장
    private String encounterId;     // 응급 접수 ID
    private String wardPref;        // 희망 병동 (공통코드 WARD_CD) — 병상 배정 시 참고
    @Column(length = 1)
    private String isolationYn;     // 격리 필요 여부 (Y/N)
    private String requestedBy;     // 입원을 요청한 응급 의사 ID (doctorId에도 우선 같은 값을 넣고, 주치의는 병동에서 정함)
    @Column(length = 500)
    private String note;            // 요청 메모 (진단명 대신)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
