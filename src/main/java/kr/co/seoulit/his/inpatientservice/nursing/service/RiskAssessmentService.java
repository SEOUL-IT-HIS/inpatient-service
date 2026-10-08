package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentHistoryDTO;

import java.util.List;

public interface RiskAssessmentService {
    /** admissionId가 있으면 그 입원 건의 기록만, 없으면 전체 */
    List<RiskAssessmentDTO> getRiskAssessments(String admissionId);

    List<RiskAssessmentHistoryDTO> getRiskAssessmentHistory(String patientRiskAssessmentId);

    RiskAssessmentDTO createRiskAssessment(RiskAssessmentDTO requestDto);

    RiskAssessmentDTO getRiskAssessment(String patientRiskAssessmentId);

    RiskAssessmentDTO updateRiskAssessment(String patientRiskAssessmentId, RiskAssessmentDTO requestDto);

    void deleteRiskAssessment(String patientRiskAssessmentId);
}
