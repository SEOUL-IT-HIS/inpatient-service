package kr.co.seoulit.his.inpatientservice.prescription.controller;

import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionCancelRequest;
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

    // 전송 실패(SEND_FAILED)한 항목만 검사실/약제부로 다시 전송 — 등록 시 전송은 서버가 자동으로 함
    @PostMapping("/{prescriptionId}/dispatch")
    public ApiResponse<PrescriptionDTO> retryDispatch(@PathVariable String prescriptionId){
        return ApiResponse.success(prescriptionService.retryDispatch(prescriptionId));
    }

    // 처방 취소 — 외래 처방코어(deactivate)에 취소 요청 후 병동 상태도 CANCELLED로 변경
    @PatchMapping("/{prescriptionId}/cancel")
    public ApiResponse<PrescriptionDTO> cancelPrescription(@PathVariable String prescriptionId,
                                                           @RequestBody PrescriptionCancelRequest request){
        return ApiResponse.success(prescriptionService.cancelPrescription(prescriptionId, request.cancelReason()));
    }

}
