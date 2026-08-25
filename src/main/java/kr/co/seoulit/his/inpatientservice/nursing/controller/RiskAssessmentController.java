package kr.co.seoulit.his.inpatientservice.nursing.controller;


import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.service.RiskAssessmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/inpatient/nursingrecord/riskassessment")
public class RiskAssessmentController {
    private final RiskAssessmentService riskAssessmentService;

    public RiskAssessmentController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }


    @GetMapping
    public ApiResponse<List<RiskAssessmentDTO>> getRiskAssessments() {
        return ApiResponse.success(riskAssessmentService.getRiskAssessments());
    }

    @GetMapping("/{riskAssessmentId}")
    public ApiResponse<RiskAssessmentDTO> getRiskAssessment(@PathVariable String riskAssessmentId) {
        return ApiResponse.success(riskAssessmentService.getRiskAssessment(riskAssessmentId));
    }

    @PostMapping
    public ApiResponse<RiskAssessmentDTO> createRiskAssessment(@RequestBody RiskAssessmentDTO requestDto) {
        return ApiResponse.success(riskAssessmentService.createRiskAssessment(requestDto));
    }

    @PutMapping("/{riskAssessmentId}")
    public ApiResponse<RiskAssessmentDTO> updateRiskAssessment(@PathVariable String riskAssessmentId,
                                                     @RequestBody RiskAssessmentDTO requestDto) {
        return ApiResponse.success(riskAssessmentService.updateRiskAssessment(riskAssessmentId, requestDto));
    }

    @DeleteMapping("/{riskAssessmentId}")
    public ApiResponse<Void> deleteRiskAssessment(@PathVariable String riskAssessmentId) {
        riskAssessmentService.deleteRiskAssessment(riskAssessmentId);
        return ApiResponse.success(null);
    }
}
