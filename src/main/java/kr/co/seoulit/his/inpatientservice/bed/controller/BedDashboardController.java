package kr.co.seoulit.his.inpatientservice.bed.controller;

import kr.co.seoulit.his.inpatientservice.bed.dto.WardBedStatusDTO;
import kr.co.seoulit.his.inpatientservice.bed.service.BedDashboardService;
import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inpatient/bed/dashboard")
@RequiredArgsConstructor
public class BedDashboardController {

    private final BedDashboardService bedDashboardService;

    @GetMapping
    public ApiResponse<WardBedStatusDTO> getWardBedStatus(@RequestParam String wardCd){
        return ApiResponse.success(bedDashboardService.getWardBedStatus(wardCd));
    }
}
