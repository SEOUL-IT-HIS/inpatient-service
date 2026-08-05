package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedReservationDTO;

import java.util.List;

public interface BedReservationService {
    List<BedReservationDTO> getBedReservations();

    BedReservationDTO createBedReservation(BedReservationDTO requestDto);

    BedReservationDTO getBedReservation(Long bedReservationId);

    BedReservationDTO updateBedReservation(Long bedReservationId, BedReservationDTO requestDto);

    void deleteBedReservation(Long bedReservationId);

    /** 이 병상에 아직 끝나지 않은(REQUESTED/RESERVED) 예약이 있는지 — 배정 쪽에서 "예약된 병상 가로채기" 방지용으로도 씀 */
    boolean hasActiveReservation(String bedId);
}
