package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;

import java.util.List;

public interface BedAssignmentService {

    List<BedAssignmentDTO> getBedAssignments();

    BedAssignmentDTO createBedAssignment(BedAssignmentDTO requestDto);

    BedAssignmentDTO getBedAssignment(String assignmentId);

    BedAssignmentDTO updateBedAssignment(String assignmentId, BedAssignmentDTO requestDto);

    void deleteBedAssignment(String assignmentId);
}
