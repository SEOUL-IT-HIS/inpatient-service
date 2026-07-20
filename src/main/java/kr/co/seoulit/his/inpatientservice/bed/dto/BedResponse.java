package kr.co.seoulit.his.inpatientservice.bed.dto;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BedResponse {

    private String bedId;
    private String roomNo;
    private String bedNo;
    private BedStatus bedStatus;
    private Long admissionId;
    private LocalDateTime assignedAt;
}
