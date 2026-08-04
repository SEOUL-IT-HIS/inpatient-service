package kr.co.seoulit.his.inpatientservice.bed.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedReservationDTO {
    private Long bedReservationId;
    private String bedId;
    private String patientId;
    private LocalDateTime reserveAt;
    private LocalDateTime expectedAdmissionAt;
    private String reservationStatusCd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
