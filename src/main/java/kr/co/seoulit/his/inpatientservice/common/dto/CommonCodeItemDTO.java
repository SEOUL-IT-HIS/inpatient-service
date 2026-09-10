package kr.co.seoulit.his.inpatientservice.common.dto;

public record CommonCodeItemDTO(
        String codeId,
        String groupId,
        String parentCodeId,
        String codeValue,
        String codeName,
        int sortOrder,
        String useYn
) {
}
