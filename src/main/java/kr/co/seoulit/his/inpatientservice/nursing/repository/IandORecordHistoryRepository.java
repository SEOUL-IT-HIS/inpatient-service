package kr.co.seoulit.his.inpatientservice.nursing.repository;

import kr.co.seoulit.his.inpatientservice.nursing.entity.IandORecordHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface IandORecordHistoryRepository extends JpaRepository<IandORecordHistoryEntity, Long> {
    List<IandORecordHistoryEntity> findByIntakeOutputIdOrderByChangedAtDesc(String intakeOutputId);
}
