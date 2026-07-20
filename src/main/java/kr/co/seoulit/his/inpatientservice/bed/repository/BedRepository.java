package kr.co.seoulit.his.inpatientservice.bed.repository;

import kr.co.seoulit.his.inpatientservice.bed.entity.Bed;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BedRepository extends JpaRepository<Bed, String> {

    Page<Bed> findByBedStatus(BedStatus bedStatus, Pageable pageable);

    @Query("SELECT b.bedStatus AS status, COUNT(b) AS count FROM Bed b GROUP BY b.bedStatus")
    List<BedStatusCount> countByStatus();
}
