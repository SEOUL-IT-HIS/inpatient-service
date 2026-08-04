package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedReservationDTO;

import java.util.List;

public interface BedReservationService {
    List<BedReservationDTO> getBedReservations();

    BedReservationDTO createBedReservation(BedReservationDTO requestDto);

    BedReservationDTO getBedReservation(Long bedReservationId);

    BedReservationDTO updateBedReservation(Long bedReservationId, BedReservationDTO requestDto);

    void deleteBedReservation(Long bedReservationId);
}
