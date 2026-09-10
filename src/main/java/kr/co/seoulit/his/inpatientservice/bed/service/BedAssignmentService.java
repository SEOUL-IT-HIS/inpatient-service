package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;

import java.util.List;

public interface BedAssignmentService {

    List<BedAssignmentDTO> getBedAssignments();

    BedAssignmentDTO createBedAssignment(BedAssignmentDTO requestDto);

    BedAssignmentDTO getBedAssignment(Long assignmentId);

    BedAssignmentDTO updateBedAssignment(Long assignmentId, BedAssignmentDTO requestDto);

    void deleteBedAssignment(Long assignmentId);

    void releaseBedByAdmissionId(String admissionId);

    String findActiveRoomTypeCode(String admissionId);
}
