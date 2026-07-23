package kr.co.seoulit.his.inpatientservice.admission.repository;

import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdmissionRepository extends JpaRepository<AdmissionEntity, String> {

}
