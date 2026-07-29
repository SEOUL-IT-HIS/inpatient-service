package kr.co.seoulit.his.inpatientservice.bed.repository;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BedRepository extends JpaRepository<BedEntity, String> {
}
