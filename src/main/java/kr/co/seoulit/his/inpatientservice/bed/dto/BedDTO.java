package kr.co.seoulit.his.inpatientservice.bed.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedDTO {
    private String bedId;
    private String wardCd;
    private String roomTypeCode;
    private String roomNo;
    private String bedNo;
    private String bedStatus;
    private String patientId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
