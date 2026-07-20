package kr.co.seoulit.his.inpatientservice.bed.repository;

import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BedAssignmentRepository extends JpaRepository<BedAssignment, Long> {

    boolean existsByAdmissionIdAndReleasedAtIsNull(Long admissionId);
}
