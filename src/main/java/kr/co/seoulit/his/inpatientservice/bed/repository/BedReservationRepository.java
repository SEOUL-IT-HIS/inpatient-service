package kr.co.seoulit.his.inpatientservice.bed.repository;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BedReservationRepository extends JpaRepository<BedReservationEntity, Long> {
    boolean existsByBedIdAndReservationStatusCdIn(String bedId, List<BedReservationStatus> statuses);

}
