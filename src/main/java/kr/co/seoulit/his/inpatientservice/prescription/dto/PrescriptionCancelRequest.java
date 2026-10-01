package kr.co.seoulit.his.inpatientservice.prescription.dto;

// 처방 취소 요청 body (프론트 → 병동). 외래로는 쿼리 파라미터로 바꿔서 전달함
public record PrescriptionCancelRequest(String cancelReason) {}
