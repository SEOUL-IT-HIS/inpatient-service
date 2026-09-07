package kr.co.seoulit.his.inpatientservice.nursing.repository;

import kr.co.seoulit.his.inpatientservice.nursing.entity.NursingAssessmentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface NursingAssessmentHistoryRepository extends JpaRepository<NursingAssessmentHistoryEntity, Long> {
    List<NursingAssessmentHistoryEntity> findByNursingAssessmentIdOrderByChangedAtDesc(String nursingAssessmentId);
}

