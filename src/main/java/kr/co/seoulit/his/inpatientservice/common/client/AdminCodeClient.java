package kr.co.seoulit.his.inpatientservice.common.client;

import kr.co.seoulit.his.inpatientservice.common.dto.AdminApiResponse;
import kr.co.seoulit.his.inpatientservice.common.dto.CommonCodeGroupDTO;
import kr.co.seoulit.his.inpatientservice.common.dto.CommonCodeItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminCodeClient {

    private final RestClient adminRestClient;

    public List<CommonCodeItemDTO> getCodesByGroupCode(String groupCode){
        String groupId = findGroupId(groupCode);

        AdminApiResponse<List<CommonCodeItemDTO>> response = adminRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/commonCodeItem/list")
                        .queryParam("groupId", groupId)
                        .build())
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<AdminApiResponse<List<CommonCodeItemDTO>>>(){});
        return response.data();

    }
    private String findGroupId(String groupCode){
        AdminApiResponse<List<CommonCodeGroupDTO>> response = adminRestClient.get()
                .uri("/api/commonCodeGroup/list")
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<AdminApiResponse<List<CommonCodeGroupDTO>>>(){});
        return response.data().stream()
                .filter(group -> group.groupCode().equals(groupCode))
                .map(CommonCodeGroupDTO::groupId)
                .findFirst()
                .orElseThrow(()->new RuntimeException("Common Code group not found"));
    }
}
