package kr.co.seoulit.his.inpatientservice.admission.event;

/**
 * 병동 → 응급 회신: 병상 배정 완료 (토픽: inpatient.admission.bed-assigned.v1, key: dispositionId)
 * 응급에서 온 입원 건(dispositionId가 있는 건)에 병상배정이 생성됐을 때 발행
 */
public record BedAssignedEvent(
        String dispositionId,       // 응급 요청 식별자 그대로
        String admissionRequestId,  // 입원 건을 만든 요청의 ID 그대로 (응급이 요청에 안 넣었으면 null)
        String wardCode,            // 배정된 병상의 병동 (BED.wardCd, 공통코드 WARD_CD 값)
        String bedId
) {}
