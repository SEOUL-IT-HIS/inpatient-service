package kr.co.seoulit.his.inpatientservice.nursing.repository;

import kr.co.seoulit.his.inpatientservice.nursing.entity.RiskAssessmentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RiskAssessmentHistoryRepository extends JpaRepository<RiskAssessmentHistoryEntity, Long> {
    List<RiskAssessmentHistoryEntity> findByPatientRiskAssessmentIdOrderByChangedAtDesc(String patientRiskAssessmentId);
}
