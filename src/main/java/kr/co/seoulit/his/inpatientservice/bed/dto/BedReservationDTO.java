package kr.co.seoulit.his.inpatientservice.bed.dto;

import lombok.*;

import java.time.LocalDateTime;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationStatus;

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
    private BedReservationStatus reservationStatusCd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
