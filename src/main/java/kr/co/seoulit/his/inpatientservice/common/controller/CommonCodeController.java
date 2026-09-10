package kr.co.seoulit.his.inpatientservice.common.controller;

import kr.co.seoulit.his.inpatientservice.common.client.AdminCodeClient;
import kr.co.seoulit.his.inpatientservice.common.dto.CommonCodeItemDTO;
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
    public List<CommonCodeItemDTO> getWardCodes(){
        return adminCodeClient.getCodesByGroupCode("WARD_CD");
    }
}
