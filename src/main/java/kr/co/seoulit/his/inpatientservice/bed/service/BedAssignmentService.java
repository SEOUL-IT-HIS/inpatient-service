package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentRequest;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentResponse;

import java.util.List;

public interface BedAssignmentService {

    List<BedAssignmentResponse> getBedAssignments();

    BedAssignmentResponse getBedAssignment(Long assignmentId);

    BedAssignmentResponse createBedAssignment(BedAssignmentRequest request);

    BedAssignmentResponse updateBedAssignment(Long assignmentId, BedAssignmentRequest request);

    BedAssignmentResponse releaseBedAssignment(Long assignmentId);
}
