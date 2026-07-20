package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedListResponse;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import org.springframework.data.domain.Pageable;

public interface BedService {

    BedListResponse getBeds(BedStatus status, Pageable pageable);
}
