package kr.co.seoulit.his.inpatientservice.bed.repository;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface BedReservationRepository extends JpaRepository<BedReservationEntity, Long> {
    boolean existsByBedIdAndReservationStatusCdIn(String bedId, List<BedReservationStatus> statuses);

    // 자동 배정 대상: 특정 상태(RESERVED)이면서 입원예정시각이 기준 시각(지금) 이전·같은 예약
    List<BedReservationEntity> findByReservationStatusCdAndExpectedAdmissionAtLessThanEqual(
            BedReservationStatus status, LocalDateTime time);

}
