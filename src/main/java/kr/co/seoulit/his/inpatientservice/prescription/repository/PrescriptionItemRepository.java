package kr.co.seoulit.his.inpatientservice.prescription.repository;

import kr.co.seoulit.his.inpatientservice.prescription.entity.PrescriptionItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItemEntity, String> {
    List<PrescriptionItemEntity> findByPrescriptionId(String prescriptionId);
}
