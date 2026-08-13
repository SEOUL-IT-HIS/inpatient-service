package kr.co.seoulit.his.inpatientservice.bed.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedReservationScheduleRequest {
    private LocalDateTime reserveAt;
    private LocalDateTime expectedAdmissionAt;
}
