package kr.co.seoulit.his.inpatientservice.bed.repository;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BedReservationRepository extends JpaRepository<BedReservationEntity, Long> {
}
