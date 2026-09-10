package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.WardBedStatusDTO;

public interface BedDashboardService {
    WardBedStatusDTO getWardBedStatus(String wardCd);
}
