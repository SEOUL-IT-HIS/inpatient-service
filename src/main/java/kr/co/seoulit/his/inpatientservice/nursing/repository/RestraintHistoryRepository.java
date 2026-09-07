package kr.co.seoulit.his.inpatientservice.nursing.repository;

import kr.co.seoulit.his.inpatientservice.nursing.entity.RestraintHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RestraintHistoryRepository extends JpaRepository<RestraintHistoryEntity, Long> {
    List<RestraintHistoryEntity> findByRestraintIdOrderByChangedAtDesc(String restraintId);
}

