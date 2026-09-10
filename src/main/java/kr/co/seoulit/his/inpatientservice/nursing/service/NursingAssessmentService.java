package kr.co.seoulit.his.inpatientservice.nursing.service;



import kr.co.seoulit.his.inpatientservice.common.aop.TracksHistory;
import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentHistoryDTO;

import java.util.List;

public interface NursingAssessmentService {
    List<NursingAssessmentDTO> getNursingAssessments();

    List<NursingAssessmentHistoryDTO> getNursingAssessmentHistory(String nursingAssessmentId);

    NursingAssessmentDTO createNursingAssessment(NursingAssessmentDTO requestDto);

    NursingAssessmentDTO getNursingAssessment(String NursingAssessmentId);

    @TracksHistory(changeType = "UPDATED")
    NursingAssessmentDTO updateNursingAssessment(String NursingAssessmentId, NursingAssessmentDTO requestDto);

    @TracksHistory(changeType = "DELETED")
    void deleteNursingAssessment(String NursingAssessmentId);
}
