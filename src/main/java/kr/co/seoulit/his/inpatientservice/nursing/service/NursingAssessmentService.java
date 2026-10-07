package kr.co.seoulit.his.inpatientservice.nursing.service;



import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentHistoryDTO;

import java.util.List;

public interface NursingAssessmentService {
    List<NursingAssessmentDTO> getNursingAssessments();

    List<NursingAssessmentHistoryDTO> getNursingAssessmentHistory(String nursingAssessmentId);

    NursingAssessmentDTO createNursingAssessment(NursingAssessmentDTO requestDto);

    NursingAssessmentDTO getNursingAssessment(String NursingAssessmentId);

    NursingAssessmentDTO updateNursingAssessment(String NursingAssessmentId, NursingAssessmentDTO requestDto);

    void deleteNursingAssessment(String NursingAssessmentId);
}
