package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.common.aop.TracksHistory;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintHistoryDTO;

import java.util.List;

public interface RestraintService {
    List<RestraintDTO> getRestraints();

    List<RestraintHistoryDTO> getRestraintHistory(String restraintId);

    RestraintDTO createRestraint(RestraintDTO requestDto);

    RestraintDTO getRestraint(String RestraintId);

    @TracksHistory(changeType = "UPDATED")
    RestraintDTO updateRestraint(String RestraintId, RestraintDTO requestDto);

    @TracksHistory(changeType = "DELETED")
    void deleteRestraint(String RestraintId);
}
