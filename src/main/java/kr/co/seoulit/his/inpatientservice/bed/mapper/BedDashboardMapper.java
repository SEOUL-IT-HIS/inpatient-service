package kr.co.seoulit.his.inpatientservice.bed.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface BedDashboardMapper {

    void getWardBedStatusSummary(Map<String, Object> param);
    List<Map<String,Object>> getWardBedDetailList(String wardCd);
}
