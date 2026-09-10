package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.common.aop.TracksHistory;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentHistoryDTO;

import java.util.List;

public interface RiskAssessmentService {
    List<RiskAssessmentDTO> getRiskAssessments();

    List<RiskAssessmentHistoryDTO> getRiskAssessmentHistory(String patientRiskAssessmentId);

    RiskAssessmentDTO createRiskAssessment(RiskAssessmentDTO requestDto);

    RiskAssessmentDTO getRiskAssessment(String patientRiskAssessmentId);

    @TracksHistory(changeType = "UPDATED")
    RiskAssessmentDTO updateRiskAssessment(String patientRiskAssessmentId, RiskAssessmentDTO requestDto);

    @TracksHistory(changeType = "DELETED")
    void deleteRiskAssessment(String patientRiskAssessmentId);
}
