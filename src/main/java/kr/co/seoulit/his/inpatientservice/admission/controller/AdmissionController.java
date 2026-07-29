package kr.co.seoulit.his.inpatientservice.admission.controller;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.service.AdmissionService;
import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admission")
public class AdmissionController {
    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @PostMapping("/reception")
    public ApiResponse<AdmissionDTO> receiveAdmission(@RequestBody AdmissionDTO requestDto){
        return ApiResponse.success(admissionService.receiveAdmission(requestDto));
    }

    @GetMapping
    public ApiResponse<List<AdmissionDTO>> getAdmissions() {
        return ApiResponse.success(admissionService.getAdmissions());
    }

    @GetMapping("/{admissionId}")
    public ApiResponse<AdmissionDTO> getAdmissions(@PathVariable String admissionId) {
        return ApiResponse.success(admissionService.getAdmission(admissionId));
    }

    @PostMapping
    public ApiResponse<AdmissionDTO> createAdmission(@RequestBody AdmissionDTO requestDto) {
        return ApiResponse.success(admissionService.createAdmission(requestDto));
    }
    @PatchMapping("/{admissionId}/status")
    public ApiResponse<AdmissionDTO> changeStatus(@PathVariable String admissionId,
                                                  @RequestBody AdmissionDTO requestDto){
        return ApiResponse.success(admissionService.changeStatus(admissionId,requestDto.getStatus()));

    }

    @PutMapping("/{admissionId}")
    public ApiResponse<AdmissionDTO> updateAdmission(@PathVariable String admissionId,
            @RequestBody AdmissionDTO requestDto) {
        return ApiResponse.success(admissionService.updateAdmission(admissionId, requestDto));
    }

}
