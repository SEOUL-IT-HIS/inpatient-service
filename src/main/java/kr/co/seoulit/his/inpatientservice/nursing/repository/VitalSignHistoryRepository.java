package kr.co.seoulit.his.inpatientservice.nursing.repository;

import kr.co.seoulit.his.inpatientservice.nursing.entity.VitalSignHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VitalSignHistoryRepository extends JpaRepository<VitalSignHistoryEntity, Long> {
    List<VitalSignHistoryEntity> findByVitalSignIdOrderByChangedAtDesc(String vitalSignId);
}