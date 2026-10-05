package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapperImpl;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 격리 환자 병실 규칙 — 격리가 필요한 입원 건은 1인실 · 격리실 · 특실만 (다인실 거절), 병상 변경에도 같은 규칙
@DataJpaTest(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.kafka.enabled=false"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({BedAssignmentServiceImpl.class, BedAssignmentMapperImpl.class})
class BedIsolationRoomTest {

    @Autowired BedAssignmentService bedAssignmentService;
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
    void 격리_환자는_다인실에_배정할_수_없고_병상도_그대로다() {
        saveAdmission("ADM-ISO", "Y");
        saveBed("BED-MULTI", "02");

        assertThatThrownBy(() -> bedAssignmentService.createBedAssignment(request("ADM-ISO", "BED-MULTI")))
                .extracting("errorCode").isEqualTo(ErrorCode.ISOLATION_ROOM_REQUIRED);

        assertThat(bedAssignmentRepository.count()).isZero();
        assertThat(bedRepository.findById("BED-MULTI").orElseThrow().getBedStatus()).isEqualTo(BedStatus.EMPTY);
    }

    @Test
    void 격리_환자는_1인실_격리실_특실에는_배정할_수_있다() {
        saveAdmission("ADM-ISO1", "Y");
        saveAdmission("ADM-ISO3", "Y");
        saveAdmission("ADM-ISO4", "Y");
        saveBed("BED-PRIVATE", "01");
        saveBed("BED-ISOLATION", "03");
        saveBed("BED-VIP", "04");

        bedAssignmentService.createBedAssignment(request("ADM-ISO1", "BED-PRIVATE"));
        bedAssignmentService.createBedAssignment(request("ADM-ISO3", "BED-ISOLATION"));
        bedAssignmentService.createBedAssignment(request("ADM-ISO4", "BED-VIP"));

        assertThat(bedRepository.findAll()).allMatch(bed -> bed.getBedStatus() == BedStatus.OCCUPIED);
    }

    @Test
    void 격리가_필요_없는_환자는_다인실에_배정된다() {
        saveAdmission("ADM-NORMAL", "N");
        saveBed("BED-MULTI", "02");

        bedAssignmentService.createBedAssignment(request("ADM-NORMAL", "BED-MULTI"));

        assertThat(bedRepository.findById("BED-MULTI").orElseThrow().getBedStatus()).isEqualTo(BedStatus.OCCUPIED);
    }

    @Test
    void 격리_환자의_병상을_다인실로_옮길_수_없고_1인실로는_옮길_수_있다() {
        saveAdmission("ADM-ISO", "Y");
        saveBed("BED-PRIVATE", "01");
        saveBed("BED-MULTI", "02");
        saveBed("BED-PRIVATE2", "01");
        BedAssignmentDTO assigned = bedAssignmentService.createBedAssignment(request("ADM-ISO", "BED-PRIVATE"));

        BedAssignmentDTO toMulti = request("ADM-ISO", "BED-MULTI");
        toMulti.setAssignedAt(assigned.getAssignedAt());
        assertThatThrownBy(() -> bedAssignmentService.updateBedAssignment(assigned.getAssignmentId(), toMulti))
                .extracting("errorCode").isEqualTo(ErrorCode.ISOLATION_ROOM_REQUIRED);
        assertThat(bedRepository.findById("BED-MULTI").orElseThrow().getBedStatus()).isEqualTo(BedStatus.EMPTY);

        BedAssignmentDTO toPrivate = request("ADM-ISO", "BED-PRIVATE2");
        toPrivate.setAssignedAt(assigned.getAssignedAt());
        bedAssignmentService.updateBedAssignment(assigned.getAssignmentId(), toPrivate);
        assertThat(bedRepository.findById("BED-PRIVATE").orElseThrow().getBedStatus()).isEqualTo(BedStatus.EMPTY);
        assertThat(bedRepository.findById("BED-PRIVATE2").orElseThrow().getBedStatus()).isEqualTo(BedStatus.OCCUPIED);
    }

    private void saveAdmission(String admissionId, String isolationYn) {
        admissionRepository.save(AdmissionEntity.builder()
                .admissionId(admissionId).patientId("P-" + admissionId).status("REQUESTED")
                .isolationYn(isolationYn).admissionDate(DateRules.now().minusHours(2)).build());
    }

    private void saveBed(String bedId, String roomTypeCode) {
        bedRepository.save(BedEntity.builder()
                .bedId(bedId).wardCd("01").roomNo("201").bedNo("A")
                .bedStatus(BedStatus.EMPTY).roomTypeCode(roomTypeCode).build());
    }

    private BedAssignmentDTO request(String admissionId, String bedId) {
        BedAssignmentDTO dto = new BedAssignmentDTO();
        dto.setAdmissionId(admissionId);
        dto.setBedId(bedId);
        dto.setAssignedAt(DateRules.now().minusMinutes(10).withNano(0));
        return dto;
    }
}
