package kr.co.seoulit.his.inpatientservice.admission.event;

/**
 * 외래 → 병동 입원요청 (토픽: outpatient.admission.requested.v1, key: requestId)
 * 검사·약제 이벤트와 같은 봉투 형식. 시각 필드는 "+09:00" 오프셋이 붙을 수 있어 String으로 받고, 저장할 때 변환
 * (응급 규격(AdmissionRequestedEvent)과는 별개 — 응급은 봉투 없는 평문 JSON)
 */
public record OpdAdmissionRequestedEvent(
        String eventId,
        String eventType,
        String version,
        String occurredAt,
        String source,
        Data data
) {
    public static final String ADMISSION_ROUTE = "Outpatient"; // 응급 건("Emergency")과 구분하는 입원 경로 값

    public record Data(
            String requestId,           // 외래 입원요청 ID (UUID) — 중복 수신 확인 · 회신 key
            String encounterId,         // 외래 진료 ID
            String patientId,
            String requestedBy,         // 요청한 외래 의사 ID
            String targetDeptCode,      // 입원 진료과 (DEPT_CD)
            String wardPrefer,          // 희망 병동 (WARD_CD, 선택)
            String preferredAdmitDate,  // 입원 희망일 "yyyy-MM-dd" (선택)
            String note                 // 선택, 500자 이내
    ) {}
}
