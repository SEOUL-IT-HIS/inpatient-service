package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedDetailDTO;
import kr.co.seoulit.his.inpatientservice.bed.dto.WardBedStatusDTO;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedDashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BedDashboardServiceImpl implements BedDashboardService {
    private final BedDashboardMapper bedDashboardMapper;

    @Override
    public WardBedStatusDTO getWardBedStatus(String wardCd){
        Map<String, Object> param = new HashMap<>();
        param.put("wardCd", wardCd);
        bedDashboardMapper.getWardBedStatusSummary(param);

        List<Map<String,Object>> rows = bedDashboardMapper.getWardBedDetailList(wardCd);
        List<BedDetailDTO> beds = rows.stream()
        .map(row -> new BedDetailDTO(
                (String) row.get("bedId"),
                (String) row.get("roomNo"),
                (String) row.get("bedNo"),
                (String) row.get("bedStatus"),
                (String) row.get("patientId")
        ))
                .toList();
        return new WardBedStatusDTO(
                ((Number) param.get("totalCnt")).intValue(),
                ((Number) param.get("emptyCnt")).intValue(),
                ((Number) param.get("occupiedCnt")).intValue(),
                ((Number) param.get("reservedCnt")).intValue(),
                ((Number) param.get("maintenanceCnt")).intValue(),
                beds
        );
    }

}
