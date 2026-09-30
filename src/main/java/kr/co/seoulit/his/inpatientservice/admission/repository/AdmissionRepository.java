package kr.co.seoulit.his.inpatientservice.admission.repository;

import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdmissionRepository extends JpaRepository<AdmissionEntity, String> {
    boolean existsByPatientIdAndStatusNot(String patientId,String status);

    // 예약 → 배정 전환 시, 예약의 patientId로 그 환자의 입원 대기(REQUESTED) 건을 찾을 때 사용
    // (환자당 진행중 입원은 1건만 허용되므로 First로 충분)
    Optional<AdmissionEntity> findFirstByPatientIdAndStatus(String patientId, String status);
}
