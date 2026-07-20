package kr.co.seoulit.his.inpatientservice.bed.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentRequest;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentResponse;
import kr.co.seoulit.his.inpatientservice.bed.service.BedAssignmentService;
import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bed-assignments")
@RequiredArgsConstructor
public class BedAssignmentController {

    private final BedAssignmentService bedAssignmentService;

    @GetMapping
    public ApiResponse<List<BedAssignmentResponse>> getBedAssignments() {
        return ApiResponse.success(bedAssignmentService.getBedAssignments());
    }

    @GetMapping("/{assignmentId}")
    public ApiResponse<BedAssignmentResponse> getBedAssignment(@PathVariable Long assignmentId) {
        return ApiResponse.success(bedAssignmentService.getBedAssignment(assignmentId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BedAssignmentResponse> createBedAssignment(@Valid @RequestBody BedAssignmentRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, bedAssignmentService.createBedAssignment(request));
    }

    @PutMapping("/{assignmentId}")
    public ApiResponse<BedAssignmentResponse> updateBedAssignment(@PathVariable Long assignmentId,
                                                                    @Valid @RequestBody BedAssignmentRequest request) {
        return ApiResponse.success(bedAssignmentService.updateBedAssignment(assignmentId, request));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND, e.getMessage()));
    }
}
