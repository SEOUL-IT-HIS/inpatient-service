package kr.co.seoulit.his.inpatientservice.bed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import kr.co.seoulit.his.inpatientservice.admission.event.OpdAdmissionResultPublisher;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignmentEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapper;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.common.util.DateRules;

// 퇴상 후 병상은 점검중(MAINTENANCE)이 되고, "청소 완료"를 눌러야 EMPTY로 돌아감
class BedMaintenanceTest {

    private final BedRepository bedRepository = mock(BedRepository.class);
    private final BedAssignmentRepository bedAssignmentRepository = mock(BedAssignmentRepository.class);

    @SuppressWarnings("unchecked")
    private final BedAssignmentServiceImpl assignmentService = new BedAssignmentServiceImpl(
            bedAssignmentRepository, mock(BedAssignmentMapper.class), bedRepository,
            mock(BedReservationService.class), mock(AdmissionRepository.class),
            mock(KafkaTemplate.class), "inpatient.admission.bed-assigned.v1", false,
            mock(OpdAdmissionResultPublisher.class));
    private final BedServiceImpl bedService = new BedServiceImpl(bedRepository, mock(BedMapper.class));

    private BedEntity bed(BedStatus status, String patientId) {
        BedEntity bed = BedEntity.builder().bedId("BED001").roomNo("201").bedNo("A")
                .bedStatus(status).patientId(patientId).build();
        when(bedRepository.findById("BED001")).thenReturn(Optional.of(bed));
        return bed;
    }

    private BedAssignmentEntity activeAssignment() {
        BedAssignmentEntity assignment = BedAssignmentEntity.builder()
                .assignmentId(1L).bedId("BED001").admissionId("ADM001")
                .assignedAt(DateRules.now().minusHours(3)).build();
        when(bedAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(bedAssignmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return assignment;
    }

    @Test
    void 퇴상_처리하면_병상이_점검중이_되고_환자가_비워진다() {
        BedEntity bed = bed(BedStatus.OCCUPIED, "PAT1");
        BedAssignmentEntity assignment = activeAssignment();
        BedAssignmentDTO request = new BedAssignmentDTO();
        request.setBedId("BED001");
        request.setAdmissionId("ADM001");
        request.setAssignedAt(assignment.getAssignedAt());
        request.setReleasedAt(DateRules.now().minusMinutes(1));

        assignmentService.updateBedAssignment(1L, request);

        assertThat(bed.getBedStatus()).isEqualTo(BedStatus.MAINTENANCE);
        assertThat(bed.getPatientId()).isNull();
        verify(bedRepository).save(bed);
    }

    @Test
    void 퇴원_확정으로_자동_퇴상돼도_병상은_점검중이_된다() {
        BedEntity bed = bed(BedStatus.OCCUPIED, "PAT1");
        BedAssignmentEntity assignment = activeAssignment();
        when(bedAssignmentRepository.findByAdmissionIdAndReleasedAtIsNull("ADM001")).thenReturn(List.of(assignment));

        assignmentService.releaseBedByAdmissionId("ADM001");

        assertThat(assignment.getReleasedAt()).isNotNull();
        assertThat(bed.getBedStatus()).isEqualTo(BedStatus.MAINTENANCE);
        assertThat(bed.getPatientId()).isNull();
    }

    @Test
    void 활성_배정을_삭제하면_쓴_적_없는_병상이라_바로_EMPTY가_된다() {
        BedEntity bed = bed(BedStatus.OCCUPIED, "PAT1");
        activeAssignment();

        assignmentService.deleteBedAssignment(1L);

        assertThat(bed.getBedStatus()).isEqualTo(BedStatus.EMPTY);
        assertThat(bed.getPatientId()).isNull();
    }

    @Test
    void 청소_완료하면_점검중_병상이_EMPTY가_된다() {
        BedEntity bed = bed(BedStatus.MAINTENANCE, null);
        when(bedRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        bedService.makeBedAvailable("BED001");

        assertThat(bed.getBedStatus()).isEqualTo(BedStatus.EMPTY);
        verify(bedRepository).save(bed);
    }

    @Test
    void 점검중이_아닌_병상은_청소_완료를_거절한다() {
        for (BedStatus status : List.of(BedStatus.OCCUPIED, BedStatus.RESERVED, BedStatus.EMPTY)) {
            BedEntity bed = bed(status, "PAT1");

            assertThatThrownBy(() -> bedService.makeBedAvailable("BED001"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.BED_NOT_IN_MAINTENANCE);
            assertThat(bed.getBedStatus()).isEqualTo(status);
        }
        verify(bedRepository, never()).save(any());
    }

    @Test
    void 없는_병상의_청소_완료는_404() {
        when(bedRepository.findById("NONE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bedService.makeBedAvailable("NONE"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BED_NOT_FOUND);
    }
}
