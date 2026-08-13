package kr.co.seoulit.his.inpatientservice.nursing.controller;

import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignDTO;
import kr.co.seoulit.his.inpatientservice.nursing.service.VitalSignService;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inpatient/nursingrecord/vitalsign")
public class VitalSignController {
    private final VitalSignService vitalSignService;

    public VitalSignController(VitalSignService vitalSignService) {
        this.vitalSignService = vitalSignService;
    }

    @GetMapping
    public ApiResponse<List<VitalSignDTO>> getVitalSigns() {
        return ApiResponse.success(vitalSignService.getVitalSigns());
    }

    @GetMapping("/{vitalSignId}")
    public ApiResponse<VitalSignDTO> getVitalSign(@PathVariable String vitalSignId) {
        return ApiResponse.success(vitalSignService.getVitalSign(vitalSignId));
    }

    @PostMapping
    public ApiResponse<VitalSignDTO> createVitalSign(@RequestBody VitalSignDTO requestDto) {
        return ApiResponse.success(vitalSignService.createVitalSign(requestDto));
    }

    @PutMapping("/{vitalSignId}")
    public ApiResponse<VitalSignDTO> updateVitalSign(@PathVariable String vitalSignId,
            @RequestBody VitalSignDTO requestDto) {
        return ApiResponse.success(vitalSignService.updateVitalSign(vitalSignId, requestDto));
    }

    @DeleteMapping("/{vitalSignId}")
    public ApiResponse<Void> deleteVitalSign(@PathVariable String vitalSignId) {
        vitalSignService.deleteVitalSign(vitalSignId);
        return ApiResponse.success(null);
    }
}
