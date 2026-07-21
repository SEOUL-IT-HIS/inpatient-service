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
    private String roomNo;
    private String bedNo;
    private String bedStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
