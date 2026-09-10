package kr.co.seoulit.his.inpatientservice.bed.dto;

import java.util.List;

public record WardBedStatusDTO(
    int totalCnt,
    int emptyCnt,
    int occupiedCnt,
    int reservedCnt,
    int maintenanceCnt,
    List<BedDetailDTO> beds){
}
