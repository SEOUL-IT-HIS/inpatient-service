package kr.co.seoulit.his.inpatientservice.bed.controller;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;
import kr.co.seoulit.his.inpatientservice.bed.service.BedAssignmentService;
import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bedassignment")
public class BedAssignmentController {
    private final BedAssignmentService bedAssignmentService;

    public BedAssignmentController(BedAssignmentService bedAssignmentService) {
        this.bedAssignmentService = bedAssignmentService;
    }

    @GetMapping
    public ApiResponse<List<BedAssignmentDTO>> getBedAssignments() {
        return ApiResponse.success(bedAssignmentService.getBedAssignments());
    }

    @GetMapping("/{assignmentId}")
    public ApiResponse<BedAssignmentDTO> getBedAssignment(@PathVariable String assignmentId) {
        return ApiResponse.success(bedAssignmentService.getBedAssignment(assignmentId));
    }

    @PostMapping
    public ApiResponse<BedAssignmentDTO> createBedAssignment(@RequestBody BedAssignmentDTO requestDto) {
        return ApiResponse.success(bedAssignmentService.createBedAssignment(requestDto));
    }

    @PutMapping("/{assignmentId}")
    public ApiResponse<BedAssignmentDTO> updateBedAssignment(@PathVariable String assignmentId, @RequestBody BedAssignmentDTO requestDto) {
        return ApiResponse.success(bedAssignmentService.updateBedAssignment(assignmentId, requestDto));
    }

    @DeleteMapping("/{assignmentId}")
    public ApiResponse<Void> deleteBedAssignment(@PathVariable String assignmentId) {
        bedAssignmentService.deleteBedAssignment(assignmentId);
        return ApiResponse.success(null);
    }
}
