package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentDTO;

import java.util.List;

public interface RiskAssessmentService {
    List<RiskAssessmentDTO> getRiskAssessments();

    RiskAssessmentDTO createRiskAssessment(RiskAssessmentDTO requestDto);

    RiskAssessmentDTO getRiskAssessment(String patientRiskAssessmentId);

    RiskAssessmentDTO updateRiskAssessment(String patientRiskAssessmentId, RiskAssessmentDTO requestDto);

    void deleteRiskAssessment(String patientRiskAssessmentId);
}
