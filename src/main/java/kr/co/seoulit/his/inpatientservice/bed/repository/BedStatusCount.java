package kr.co.seoulit.his.inpatientservice.bed.repository;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;

public interface BedStatusCount {

    BedStatus getStatus();

    Long getCount();
}
