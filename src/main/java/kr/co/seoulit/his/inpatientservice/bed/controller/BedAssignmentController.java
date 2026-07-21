package kr.co.seoulit.his.inpatientservice.bed.controller;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;
import kr.co.seoulit.his.inpatientservice.bed.service.BedAssignmentService;

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
    public List<BedAssignmentDTO> getBedAssignments() {
        return bedAssignmentService.getBedAssignments();
    }

    @GetMapping("/{assignmentId}")
    public BedAssignmentDTO getBedAssignment(@PathVariable String assignmentId) {
        return bedAssignmentService.getBedAssignment(assignmentId);
    }

    @PostMapping
    public BedAssignmentDTO createBedAssignment(@RequestBody BedAssignmentDTO requestDto) {
        return bedAssignmentService.createBedAssignment(requestDto);
    }

    @PutMapping("/{assignmentId}")
    public BedAssignmentDTO updateBedAssignment(@PathVariable String assignmentId, @RequestBody BedAssignmentDTO requestDto) {
        return bedAssignmentService.updateBedAssignment(assignmentId, requestDto);
    }

    @DeleteMapping("/{assignmentId}")
    public void deleteBedAssignment(@PathVariable String assignmentId) {
        bedAssignmentService.deleteBedAssignment(assignmentId);
    }
}
