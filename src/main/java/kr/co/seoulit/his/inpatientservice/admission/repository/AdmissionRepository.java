package kr.co.seoulit.his.inpatientservice.admission.repository;

import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdmissionRepository extends JpaRepository<AdmissionEntity, String> {
    // 이 환자에게 진행중(퇴원 전) 입원이 있는지 — 중복 입원 방지
    // - status가 NULL인 건도 진행중으로 봄 (SQL의 status <> 'DISCHARGED'는 NULL 행을 빼 버려서, 상태 없는 입원이 중복 검사를 피해 갔음)
    // - excludeAdmissionId: 수정할 때 자기 자신은 빼고 확인 (등록일 때는 null)
    @Query("select count(a) > 0 from AdmissionEntity a"
            + " where a.patientId = :patientId"
            + " and (a.status is null or a.status <> 'DISCHARGED')"
            + " and (:excludeAdmissionId is null or a.admissionId <> :excludeAdmissionId)")
    boolean existsActiveAdmission(@Param("patientId") String patientId,
                                  @Param("excludeAdmissionId") String excludeAdmissionId);

    // 예약 → 배정 전환 시, 예약의 patientId로 그 환자의 입원 대기(REQUESTED) 건을 찾을 때 사용
    // (환자당 진행중 입원은 1건만 허용되므로 First로 충분)
    Optional<AdmissionEntity> findFirstByPatientIdAndStatus(String patientId, String status);

    // 응급 입원요청 중복 수신 확인 — 같은 dispositionId로 이미 만든 입원 건이 있으면 무시
    boolean existsByDispositionId(String dispositionId);
}
