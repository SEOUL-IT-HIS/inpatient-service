package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentHistoryDTO;

import java.util.List;

public interface RiskAssessmentService {
    List<RiskAssessmentDTO> getRiskAssessments();

    List<RiskAssessmentHistoryDTO> getRiskAssessmentHistory(String patientRiskAssessmentId);

    RiskAssessmentDTO createRiskAssessment(RiskAssessmentDTO requestDto);

    RiskAssessmentDTO getRiskAssessment(String patientRiskAssessmentId);

    RiskAssessmentDTO updateRiskAssessment(String patientRiskAssessmentId, RiskAssessmentDTO requestDto);

    void deleteRiskAssessment(String patientRiskAssessmentId);
}
