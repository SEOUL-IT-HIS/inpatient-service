package kr.co.seoulit.his.inpatientservice.admission.controller;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.service.AdmissionService;
import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inpatient/admission")
public class AdmissionController {
    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    // [사용 중지] 외부 서비스(원무 등)가 REST로 입원요청을 보내는 용도로 만들었으나 호출하는 곳이 없음
    // - 원무와는 연동하지 않음, 응급 입원요청은 Kafka(emergency.admission.requested.v1)로 받기로 함
    //   (개발표준 21.3: 조회 외 서비스 간 연동은 Kafka)
    // - 병동 직접 등록은 아래 createAdmission(POST /api/inpatient/admission) 사용
    // @PostMapping("/reception")
    // public ApiResponse<AdmissionDTO> receiveAdmission(@RequestBody AdmissionDTO requestDto){
    //     return ApiResponse.success(admissionService.receiveAdmission(requestDto));
    // }

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
