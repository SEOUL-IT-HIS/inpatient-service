package kr.co.seoulit.his.inpatientservice.common.controller;

import kr.co.seoulit.his.inpatientservice.common.client.AdminCodeClient;
import kr.co.seoulit.his.inpatientservice.common.dto.CommonCodeItemDTO;
import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inpatient/codes")
@RequiredArgsConstructor
public class CommonCodeController {

    private final AdminCodeClient adminCodeClient;

    @GetMapping("/ward")
    public ApiResponse<List<CommonCodeItemDTO>> getWardCodes(){
        return ApiResponse.success(adminCodeClient.getCodesByGroupCode("WARD_CD"));
    }
}
