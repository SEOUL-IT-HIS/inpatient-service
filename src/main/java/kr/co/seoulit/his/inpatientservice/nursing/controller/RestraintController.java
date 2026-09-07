package kr.co.seoulit.his.inpatientservice.nursing.controller;

import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;

import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.service.RestraintService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inpatient/nursingrecord/restraint")
public class RestraintController {
    private final RestraintService restraintService;

    public RestraintController(RestraintService restraintService) {
        this.restraintService = restraintService;
    }

    @GetMapping("/{restraintId}/history")
    public ApiResponse<List<RestraintHistoryDTO>> getRestraintHistory(@PathVariable String restraintId) {
        return ApiResponse.success(restraintService.getRestraintHistory(restraintId));
    }

    @GetMapping
    public ApiResponse<List<RestraintDTO>> getRestraints() {
        return ApiResponse.success(restraintService.getRestraints());
    }

    @GetMapping("/{restraintId}")
    public ApiResponse<RestraintDTO> getRestraint(@PathVariable String restraintId) {
        return ApiResponse.success(restraintService.getRestraint(restraintId));
    }

    @PostMapping
    public ApiResponse<RestraintDTO> createRestraint(@RequestBody RestraintDTO requestDto) {
        return ApiResponse.success(restraintService.createRestraint(requestDto));
    }

    @PutMapping("/{restraintId}")
    public ApiResponse<RestraintDTO> updateRestraint(@PathVariable String restraintId,
                                                               @RequestBody RestraintDTO requestDto) {
        return ApiResponse.success(restraintService.updateRestraint(restraintId, requestDto));
    }

    @DeleteMapping("/{restraintId}")
    public ApiResponse<Void> deleteRestraint(@PathVariable String restraintId) {
        restraintService.deleteRestraint(restraintId);
        return ApiResponse.success(null);
    }
}
