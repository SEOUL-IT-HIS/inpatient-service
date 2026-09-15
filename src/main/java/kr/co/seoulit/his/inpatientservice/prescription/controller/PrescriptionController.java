package kr.co.seoulit.his.inpatientservice.prescription.controller;

import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionCreateDTO;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionDTO;
import kr.co.seoulit.his.inpatientservice.prescription.service.PrescriptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/inpatient/prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/admission/{admissionId}")
    public ApiResponse<PrescriptionDTO> createPrescription(@PathVariable String admissionId, @RequestBody PrescriptionCreateDTO requestDto) {
        return ApiResponse.success(prescriptionService.createPrescription(admissionId,requestDto));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<List<PrescriptionDTO>> getPrescriptionsByAdmission(@PathVariable String admissionId){
        return ApiResponse.success(prescriptionService.getPrescriptionsByAdmission(admissionId));
    }
    @GetMapping("/{prescriptionId}")
    public ApiResponse<PrescriptionDTO> getPrescription(@PathVariable String prescriptionId){
        return ApiResponse.success(prescriptionService.getPrescription(prescriptionId));
    }

}
