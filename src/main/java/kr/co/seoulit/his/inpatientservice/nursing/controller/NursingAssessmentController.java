package kr.co.seoulit.his.inpatientservice.nursing.controller;

import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;

import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.service.NursingAssessmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inpatient/nursingrecord/nursingassessment")
public class NursingAssessmentController {
    private final NursingAssessmentService nursingAssessmentService;

    public NursingAssessmentController(NursingAssessmentService nursingAssessmentService) {
        this.nursingAssessmentService = nursingAssessmentService;
    }

    @GetMapping("/{nursingAssessmentId}/history")
    public ApiResponse<List<NursingAssessmentHistoryDTO>> getNursingAssessmentHistory(@PathVariable String nursingAssessmentId) {
        return ApiResponse.success(nursingAssessmentService.getNursingAssessmentHistory(nursingAssessmentId));
    }

    @GetMapping
    public ApiResponse<List<NursingAssessmentDTO>> getNursingAssessments() {
        return ApiResponse.success(nursingAssessmentService.getNursingAssessments());
    }

    @GetMapping("/{nursingAssessmentId}")
    public ApiResponse<NursingAssessmentDTO> getNursingAssessment(@PathVariable String nursingAssessmentId) {
        return ApiResponse.success(nursingAssessmentService.getNursingAssessment(nursingAssessmentId));
    }

    @PostMapping
    public ApiResponse<NursingAssessmentDTO> createNursingAssessment(@RequestBody NursingAssessmentDTO requestDto) {
        return ApiResponse.success(nursingAssessmentService.createNursingAssessment(requestDto));
    }

    @PutMapping("/{nursingAssessmentId}")
    public ApiResponse<NursingAssessmentDTO> updateNursingAssessment(@PathVariable String nursingAssessmentId,
                                                     @RequestBody NursingAssessmentDTO requestDto) {
        return ApiResponse.success(nursingAssessmentService.updateNursingAssessment(nursingAssessmentId, requestDto));
    }

    @DeleteMapping("/{nursingAssessmentId}")
    public ApiResponse<Void> deleteNursingAssessment(@PathVariable String nursingAssessmentId) {
        nursingAssessmentService.deleteNursingAssessment(nursingAssessmentId);
        return ApiResponse.success(null);
    }
}
