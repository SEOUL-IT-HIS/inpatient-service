package kr.co.seoulit.his.inpatientservice.prescription.repository;

import kr.co.seoulit.his.inpatientservice.prescription.entity.PrescriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<PrescriptionEntity, String> {
    List<PrescriptionEntity> findByAdmissionId(String admissionId);
}
