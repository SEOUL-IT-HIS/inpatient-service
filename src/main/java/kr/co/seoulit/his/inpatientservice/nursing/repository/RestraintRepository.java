package kr.co.seoulit.his.inpatientservice.nursing.repository;


import kr.co.seoulit.his.inpatientservice.nursing.entity.RestraintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestraintRepository extends JpaRepository<RestraintEntity, String> {
}
