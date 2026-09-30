package kr.co.seoulit.his.inpatientservice.bed.entity;

// 흐름: REQUESTED(요청) → RESERVED(확정) → ASSIGNED(입원예정시각이 되어 병상배정으로 전환됨)
// 중간에 취소되면 RELEASED (RELEASED는 병상을 EMPTY로 되돌리므로, 배정 완료와 구분하려고 ASSIGNED를 따로 둠)
public enum BedReservationStatus {
    REQUESTED, RELEASED, RESERVED, ASSIGNED
}
