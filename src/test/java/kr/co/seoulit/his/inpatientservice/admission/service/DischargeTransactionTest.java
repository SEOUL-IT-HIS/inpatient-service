package kr.co.seoulit.his.inpatientservice.admission.service;

import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.admission.mapper.AdmissionMapperImpl;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignmentEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapperImpl;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.bed.service.BedAssignmentServiceImpl;
import kr.co.seoulit.his.inpatientservice.bed.service.BedReservationService;
import kr.co.seoulit.his.inpatientservice.admission.event.BillingChargeEvent;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// 퇴원 확정/퇴원신청 트랜잭션 검증 — 메모리 DB(H2)에서 서비스 + JPA만 띄움 (공용 Oracle DB는 건드리지 않음)
// NOT_SUPPORTED: @DataJpaTest 기본값(테스트 전체를 트랜잭션으로 감싸고 롤백)을 끄고, 서비스의 @Transactional이 실제로 커밋/롤백하게 함
@DataJpaTest(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.kafka.enabled=true"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({AdmissionServiceImpl.class, BedAssignmentServiceImpl.class, AdmissionMapperImpl.class, BedAssignmentMapperImpl.class})
class DischargeTransactionTest {

    @Autowired AdmissionService admissionService;
    @Autowired AdmissionRepository admissionRepository;
    @Autowired BedRepository bedRepository;
    @Autowired BedAssignmentRepository bedAssignmentRepository;

    @MockitoBean KafkaTemplate<String, Object> kafkaTemplate;
    @MockitoBean BedReservationService bedReservationService;

    @AfterEach
    void cleanUp() {
        bedAssignmentRepository.deleteAll();
        bedRepository.deleteAll();
        admissionRepository.deleteAll();
    }

    @Test
    void 퇴원확정_성공하면_입원상태와_병상해제가_함께_반영된다() {
        saveAdmission("ADM001", "DISCHARGE_REQUESTED");
        saveBed("BED001", BedStatus.OCCUPIED);
        Long assignmentId = saveActiveAssignment("BED001", "ADM001");

        admissionService.changeStatus("ADM001", "DISCHARGED");

        assertThat(admissionRepository.findById("ADM001").orElseThrow().getStatus()).isEqualTo("DISCHARGED");
        assertThat(bedAssignmentRepository.findById(assignmentId).orElseThrow().getReleasedAt()).isNotNull();
        assertThat(bedRepository.findById("BED001").orElseThrow().getBedStatus()).isEqualTo(BedStatus.EMPTY);
    }

    @Test
    void 병상해제_중_실패하면_입원상태와_배정퇴상까지_롤백된다() {
        saveAdmission("ADM002", "DISCHARGE_REQUESTED");
        // 배정은 있는데 병상 row가 없음 → 배정 퇴상 저장 후 markBedEmpty에서 BED_NOT_FOUND 발생
        Long assignmentId = saveActiveAssignment("BED_MISSING", "ADM002");

        assertThatThrownBy(() -> admissionService.changeStatus("ADM002", "DISCHARGED"))
                .isInstanceOf(BusinessException.class);

        assertThat(admissionRepository.findById("ADM002").orElseThrow().getStatus()).isEqualTo("DISCHARGE_REQUESTED");
        assertThat(bedAssignmentRepository.findById(assignmentId).orElseThrow().getReleasedAt()).isNull();
    }

    @Test
    void 퇴원신청_성공하면_커밋_후_청구_이벤트_2건이_발행된다() {
        saveAdmission("ADM003", "ADMITTED");
        saveBed("BED003", BedStatus.OCCUPIED);
        saveActiveAssignment("BED003", "ADM003");

        admissionService.changeStatus("ADM003", "DISCHARGE_REQUESTED");

        assertThat(admissionRepository.findById("ADM003").orElseThrow().getStatus()).isEqualTo("DISCHARGE_REQUESTED");
        verify(kafkaTemplate, times(2)).send(any(String.class), eq("ADM003"), any());
    }

    @Test
    void 퇴원신청_검증_실패하면_상태도_이벤트도_반영되지_않는다() {
        saveAdmission("ADM004", "ADMITTED"); // 활성 병상배정 없음 → BED_ASSIGNMENT_NOT_FOUND

        assertThatThrownBy(() -> admissionService.changeStatus("ADM004", "DISCHARGE_REQUESTED"))
                .isInstanceOf(BusinessException.class);

        assertThat(admissionRepository.findById("ADM004").orElseThrow().getStatus()).isEqualTo("ADMITTED");
        verify(kafkaTemplate, never()).send(any(String.class), any(), any());
    }

    @Test
    void 특실_병상_환자가_퇴원신청하면_특실_수가코드로_청구된다() {
        saveAdmission("ADM005", "ADMITTED");
        saveBed("BED005", BedStatus.OCCUPIED, "04");
        saveActiveAssignment("BED005", "ADM005");

        admissionService.changeStatus("ADM005", "DISCHARGE_REQUESTED");

        verify(kafkaTemplate).send(any(String.class), eq("ADM005"),
                argThat(event -> event instanceof BillingChargeEvent e && "FEE013".equals(e.feeCode())));
    }

    @Test
    void 수가코드가_없는_격리실_병상은_퇴원신청을_거절한다() {
        saveAdmission("ADM006", "ADMITTED");
        saveBed("BED006", BedStatus.OCCUPIED, "03");
        saveActiveAssignment("BED006", "ADM006");

        assertThatThrownBy(() -> admissionService.changeStatus("ADM006", "DISCHARGE_REQUESTED"))
                .extracting("errorCode").isEqualTo(ErrorCode.ROOM_TYPE_FEE_CODE_NOT_MAPPED);
        assertThat(admissionRepository.findById("ADM006").orElseThrow().getStatus()).isEqualTo("ADMITTED");
    }

    private void saveAdmission(String admissionId, String status) {
        admissionRepository.save(AdmissionEntity.builder()
                .admissionId(admissionId)
                .patientId("P-" + admissionId)
                .admissionDate(LocalDateTime.now().minusDays(2))
                .status(status)
                .build());
    }

    private void saveBed(String bedId, BedStatus status) {
        saveBed(bedId, status, "01");
    }

    private void saveBed(String bedId, BedStatus status, String roomTypeCode) {
        bedRepository.save(BedEntity.builder()
                .bedId(bedId)
                .roomNo("101")
                .bedNo("1")
                .bedStatus(status)
                .roomTypeCode(roomTypeCode)
                .build());
    }

    private Long saveActiveAssignment(String bedId, String admissionId) {
        return bedAssignmentRepository.save(BedAssignmentEntity.builder()
                .bedId(bedId)
                .admissionId(admissionId)
                .assignedAt(LocalDateTime.now().minusDays(2))
                .build()).getAssignmentId();
    }
}
