package kr.co.seoulit.his.inpatientservice.nursing.repository;

import kr.co.seoulit.his.inpatientservice.nursing.entity.IandORecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IandORecordRepository extends JpaRepository<IandORecordEntity, String> {
}
