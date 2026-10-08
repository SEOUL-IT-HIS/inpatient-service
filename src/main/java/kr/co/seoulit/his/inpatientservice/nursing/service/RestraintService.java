package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintHistoryDTO;

import java.util.List;

public interface RestraintService {
    /** admissionId가 있으면 그 입원 건의 기록만, 없으면 전체 */
    List<RestraintDTO> getRestraints(String admissionId);

    List<RestraintHistoryDTO> getRestraintHistory(String restraintId);

    RestraintDTO createRestraint(RestraintDTO requestDto);

    RestraintDTO getRestraint(String RestraintId);

    RestraintDTO updateRestraint(String RestraintId, RestraintDTO requestDto);

    void deleteRestraint(String RestraintId);
}
