package kr.co.seoulit.his.inpatientservice.bed.dto;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class BedListResponse {

    private List<BedResponse> beds;
    private Map<BedStatus, Long> statusCounts;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
