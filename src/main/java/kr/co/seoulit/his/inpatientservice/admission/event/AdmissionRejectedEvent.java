package kr.co.seoulit.his.inpatientservice.admission.event;

/**
 * 병동 → 응급 회신: 입원요청 거절 (토픽: inpatient.admission.rejected.v1, key: dispositionId)
 * 병동 직원이 직접 거절하는 기능은 없음 — 입원요청을 받을 수 없을 때(예: 이미 입원 중인 환자) 자동 발행
 */
public record AdmissionRejectedEvent(
        String dispositionId,   // 응급 요청 식별자 그대로
        String rejectReason
) {}
