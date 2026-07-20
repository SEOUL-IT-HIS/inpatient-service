package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedResponse;

import java.util.List;

public interface BedService {

    List<BedResponse> getBeds();
}
