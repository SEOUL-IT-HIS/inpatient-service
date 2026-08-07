package kr.co.seoulit.his.inpatientservice.bed.repository;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BedReservationHistoryRepository extends JpaRepository<BedReservationHistoryEntity, Long> {

}
