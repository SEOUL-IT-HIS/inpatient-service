package kr.co.seoulit.his.inpatientservice.bed.controller;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedDTO;
import kr.co.seoulit.his.inpatientservice.bed.service.BedService;
import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bed")
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @GetMapping
    public ApiResponse<List<BedDTO>> getBeds() {
        return ApiResponse.success(bedService.getBeds());
    }

    @GetMapping("/{bedId}")
    public ApiResponse<BedDTO> getBed(@PathVariable String bedId) {
        return ApiResponse.success(bedService.getBed(bedId));
    }

}
