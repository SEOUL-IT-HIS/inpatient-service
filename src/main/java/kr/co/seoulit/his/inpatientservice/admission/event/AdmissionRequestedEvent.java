package kr.co.seoulit.his.inpatientservice.admission.event;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;

import java.time.LocalDateTime;

/**
 * 응급 → 병동 입원요청 (토픽: emergency.admission.requested.v1, key: dispositionId)
 * 응급팀과 합의한 규격 그대로. requestedAt은 타임존 없는 ISO 형식("2026-10-01T14:30:00")
 */
public record AdmissionRequestedEvent(
        String dispositionId,   // 요청 식별자 — 회신에 그대로 돌려줌 (거부 후 재요청 시에도 같은 값)
        String admissionRequestId, // 요청 1건마다 새로 만드는 ID — 회신에 그대로 돌려주면 응급이 정확한 요청에 반영 (없을 수 있음)
        String encounterId,     // 응급 접수 ID
        String patientId,
        String targetDeptCode,  // 진료과 (공통코드 DEPT_CD)
        String wardPref,        // 희망 병동 (공통코드 WARD_CD)
        String isolationYn,     // 격리 필요 여부 (Y/N)
        String requestedBy,     // 입원을 결정한 응급 의사 ID
        LocalDateTime requestedAt,
        String note             // 선택, 500자 이내
) {
    public static final String ADMISSION_ROUTE = "Emergency";   // 프론트 입원 등록 폼의 경로 값과 동일
    public static final String INITIAL_STATUS = "REQUESTED";    // 병동 입원 대기 → 입원요청 목록(Assignment Needed)에 표시됨

    /**
     * 입원 건 생성용 DTO로 변환 — AdmissionService.createAdmission에 그대로 넘기면 됨
     * 응급에서 주치의를 줄 수 없으므로 doctorId에는 우선 요청 의사를 넣음 (주치의는 병동에서 정함)
     */
    public AdmissionDTO toAdmissionDto() {
        return AdmissionDTO.builder()
                .patientId(patientId)
                .doctorId(requestedBy)
                .admissionDate(requestedAt)
                .admissionRoute(ADMISSION_ROUTE)
                .admissionDeptId(targetDeptCode)
                .status(INITIAL_STATUS)
                .dispositionId(dispositionId)
                .admissionRequestId(admissionRequestId)
                .encounterId(encounterId)
                .wardPref(wardPref)
                .isolationYn(isolationYn)
                .requestedBy(requestedBy)
                .note(note)
                .build();
    }
}
