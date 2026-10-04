package kr.co.seoulit.his.inpatientservice.admission.service;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.admission.mapper.AdmissionMapperImpl;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapperImpl;
import kr.co.seoulit.his.inpatientservice.bed.service.BedAssignmentServiceImpl;
import kr.co.seoulit.his.inpatientservice.bed.service.BedReservationService;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.common.util.DateRules;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 입원 등록/수정 검증 — 입원일(미래 불가, 비면 지금), 상태 기본값, 중복 입원(NULL 상태 · 수정 우회 포함)
@DataJpaTest(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.kafka.enabled=false"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({AdmissionServiceImpl.class, BedAssignmentServiceImpl.class, AdmissionMapperImpl.class, BedAssignmentMapperImpl.class})
class AdmissionRegistrationTest {

    @Autowired AdmissionService admissionService;
    @Autowired AdmissionRepository admissionRepository;

    @MockitoBean KafkaTemplate<String, Object> kafkaTemplate;
    @MockitoBean BedReservationService bedReservationService;

    @AfterEach
    void cleanUp() {
        admissionRepository.deleteAll();
    }

    // ---------- 등록 ----------

    @Test
    void 상태와_입원일_없이_등록하면_REQUESTED와_지금으로_채워진다() {
        AdmissionDTO created = admissionService.createAdmission(request("P1", null, null));

        assertThat(created.getStatus()).isEqualTo("REQUESTED");
        assertThat(created.getAdmissionDate()).isNotNull();
        assertThat(created.getAdmissionDate().toLocalDate()).isEqualTo(DateRules.today());
    }

    @Test
    void 상태_없이_등록한_환자도_진행중으로_보고_두번째_입원을_거절한다() {
        admissionService.createAdmission(request("P2", null, null));

        assertRejected(() -> admissionService.createAdmission(request("P2", null, null)), ErrorCode.ADMISSION_ALREADY_ACTIVE);
    }

    @Test
    void DB에_상태가_NULL로_남아_있는_입원도_진행중으로_본다() {
        // 예전 코드로 상태 없이 저장된 데이터
        admissionRepository.save(AdmissionEntity.builder().admissionId("A900").patientId("P3").build());

        assertRejected(() -> admissionService.createAdmission(request("P3", "REQUESTED", null)), ErrorCode.ADMISSION_ALREADY_ACTIVE);
    }

    @Test
    void 미래_입원일로는_등록할_수_없다() {
        assertRejected(() -> admissionService.createAdmission(request("P4", "REQUESTED", DateRules.now().plusDays(1))),
                ErrorCode.ADMISSION_DATE_INVALID);
        assertThat(admissionRepository.count()).isZero();
    }

    @Test
    void 퇴원한_환자는_다시_입원할_수_있다() {
        saveAdmission("A901", "P5", "DISCHARGED");

        assertThat(admissionService.createAdmission(request("P5", "REQUESTED", null)).getStatus()).isEqualTo("REQUESTED");
    }

    // ---------- 수정(PUT) ----------

    @Test
    void 퇴원_건을_다시_활성으로_되돌려_중복을_만들_수_없다() {
        saveAdmission("A910", "P6", "DISCHARGED");
        saveAdmission("A911", "P6", "ADMITTED");

        assertRejected(() -> admissionService.updateAdmission("A910", request("P6", "ADMITTED", null)),
                ErrorCode.ADMISSION_ALREADY_ACTIVE);
        assertThat(admissionRepository.findById("A910").orElseThrow().getStatus()).isEqualTo("DISCHARGED");
    }

    @Test
    void 진행중_입원이_있는_다른_환자로_바꿀_수_없다() {
        saveAdmission("A920", "P7", "ADMITTED");
        saveAdmission("A921", "P8", "ADMITTED");

        assertRejected(() -> admissionService.updateAdmission("A920", request("P8", "ADMITTED", null)),
                ErrorCode.ADMISSION_ALREADY_ACTIVE);
    }

    @Test
    void 자기_자신은_중복으로_보지_않고_빈_상태는_기존_값을_유지한다() {
        saveAdmission("A930", "P9", "ADMITTED");
        LocalDateTime newDate = DateRules.now().minusDays(2);

        AdmissionDTO updated = admissionService.updateAdmission("A930", request("P9", null, newDate));

        assertThat(updated.getStatus()).isEqualTo("ADMITTED");
        assertThat(updated.getAdmissionDate()).isEqualTo(newDate);
    }

    @Test
    void 수정으로도_미래_입원일은_넣을_수_없다() {
        saveAdmission("A940", "P10", "ADMITTED");

        assertRejected(() -> admissionService.updateAdmission("A940", request("P10", "ADMITTED", DateRules.now().plusDays(3))),
                ErrorCode.ADMISSION_DATE_INVALID);
    }

    private void saveAdmission(String admissionId, String patientId, String status) {
        admissionRepository.save(AdmissionEntity.builder()
                .admissionId(admissionId).patientId(patientId).status(status)
                .admissionDate(DateRules.now().minusDays(5)).build());
    }

    private AdmissionDTO request(String patientId, String status, LocalDateTime admissionDate) {
        AdmissionDTO dto = new AdmissionDTO();
        dto.setPatientId(patientId);
        dto.setStatus(status);
        dto.setAdmissionDate(admissionDate);
        return dto;
    }

    private void assertRejected(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(expected);
    }
}
