package kr.co.seoulit.his.inpatientservice.prescription.service;

import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionCreateDTO;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionDTO;
import kr.co.seoulit.his.inpatientservice.prescription.event.LabResultReportedEvent;

import java.util.List;

public interface PrescriptionService {
    PrescriptionDTO createPrescription(String admissionId, PrescriptionCreateDTO requestDto);

    List<PrescriptionDTO> getPrescriptionsByAdmission(String admissionId);

    PrescriptionDTO getPrescription(String prescriptionId);

    // 전송에 실패한(또는 아직 안 보낸) 항목만 다시 검사실/약제부로 전송
    PrescriptionDTO retryDispatch(String prescriptionId);

    // 처방 취소 — 외래 처방코어에 취소 요청 후 병동 DB 상태도 취소로 변경
    PrescriptionDTO cancelPrescription(String prescriptionId, String cancelReason);

    // 검사 결과 수신 — 병동 DB에 있는 입원 처방이면 항목에 결과 저장 (외래 처방 등 우리 것이 아니면 무시)
    void applyLabResult(LabResultReportedEvent event);
}
