package kr.co.seoulit.his.inpatientservice.nursing.repository;

import java.util.List;
import kr.co.seoulit.his.inpatientservice.nursing.entity.VitalSignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VitalSignRepository extends JpaRepository<VitalSignEntity, String> {
    // 입원 건별 조회 — 간호기록 화면에서 선택한 환자의 기록만 받기 위함 (전체 조회는 findAll)
    List<VitalSignEntity> findByAdmissionId(String admissionId);
}
