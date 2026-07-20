package kr.co.seoulit.his.inpatientservice.bed.controller;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedResponse;
import kr.co.seoulit.his.inpatientservice.bed.service.BedService;
import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/beds")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @GetMapping
    public ApiResponse<List<BedResponse>> getBeds() {
        return ApiResponse.success(bedService.getBeds());
    }
}
