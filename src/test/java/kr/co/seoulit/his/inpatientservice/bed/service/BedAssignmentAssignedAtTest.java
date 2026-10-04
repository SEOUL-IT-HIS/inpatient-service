package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignmentEntity;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.common.util.DateRules;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 병상배정 날짜 검증 — 배정일시는 "오늘 00:00 ~ 지금", 퇴상일시는 "배정일시 ~ 지금"
class BedAssignmentAssignedAtTest {

    private final BedAssignmentRepository bedAssignmentRepository = mock(BedAssignmentRepository.class);
    private final BedRepository bedRepository = mock(BedRepository.class);

    @SuppressWarnings("unchecked")
    private final BedAssignmentServiceImpl service = new BedAssignmentServiceImpl(
            bedAssignmentRepository, mock(BedAssignmentMapper.class), bedRepository,
            mock(BedReservationService.class), mock(AdmissionRepository.class),
            mock(KafkaTemplate.class), "inpatient.admission.bed-assigned.v1", false);

    private static final LocalDate TODAY = DateRules.today();

    @Test
    void 어제_날짜로_배정하면_거절되고_DB는_건드리지_않는다() {
        assertRejected(() -> service.createBedAssignment(request(TODAY.minusDays(1).atTime(23, 59), null)),
                ErrorCode.BED_ASSIGNED_AT_INVALID);
        verify(bedAssignmentRepository, never()).findByAdmissionIdAndReleasedAtIsNull(any());
        verify(bedAssignmentRepository, never()).save(any());
    }

    @Test
    void 배정일시가_비어_있으면_거절된다() {
        assertRejected(() -> service.createBedAssignment(request(null, null)), ErrorCode.BED_ASSIGNED_AT_INVALID);
    }

    @Test
    void 미래_날짜로_배정하면_거절된다() {
        assertRejected(() -> service.createBedAssignment(request(TODAY.plusDays(1).atTime(12, 0), null)),
                ErrorCode.BED_ASSIGNED_AT_INVALID);
        assertRejected(() -> service.createBedAssignment(request(TODAY.plusYears(2).atTime(12, 0), null)),
                ErrorCode.BED_ASSIGNED_AT_INVALID);
    }

    @Test
    void 오늘_0시_배정은_날짜_검증을_통과한다() {
        // 날짜 검증을 통과하면 다음 단계(병상 조회)로 넘어감 — 병상이 없으니 BED_NOT_FOUND가 나와야 정상
        assertRejected(() -> service.createBedAssignment(request(TODAY.atStartOfDay(), null)), ErrorCode.BED_NOT_FOUND);
        verify(bedAssignmentRepository).findByAdmissionIdAndReleasedAtIsNull("ADM001");
    }

    @Test
    void 퇴상일시가_배정일시보다_빠르면_거절된다() {
        givenExistingAssignment();
        LocalDateTime assignedAt = TODAY.atTime(0, 30);
        assertRejected(() -> service.updateBedAssignment(1L, request(assignedAt, assignedAt.minusMinutes(1))),
                ErrorCode.BED_RELEASED_AT_INVALID);
    }

    @Test
    void 퇴상일시가_미래면_거절된다() {
        givenExistingAssignment();
        assertRejected(() -> service.updateBedAssignment(1L, request(TODAY.atStartOfDay(), DateRules.now().plusHours(1))),
                ErrorCode.BED_RELEASED_AT_INVALID);
    }

    @Test
    void 지금_퇴상하는_것은_검증을_통과한다() {
        givenExistingAssignment();
        // 검증 통과 후 병상 EMPTY 처리 단계에서 병상이 없어 BED_NOT_FOUND — 날짜 검증은 통과한 것
        assertRejected(() -> service.updateBedAssignment(1L, request(TODAY.minusDays(3).atTime(9, 0), DateRules.now())),
                ErrorCode.BED_NOT_FOUND);
    }

    private void givenExistingAssignment() {
        BedAssignmentEntity existing = BedAssignmentEntity.builder()
                .assignmentId(1L).bedId("BED001").admissionId("ADM001").build();
        when(bedAssignmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bedAssignmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertRejected(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(expected);
    }

    private BedAssignmentDTO request(LocalDateTime assignedAt, LocalDateTime releasedAt) {
        BedAssignmentDTO dto = new BedAssignmentDTO();
        dto.setBedId("BED001");
        dto.setAdmissionId("ADM001");
        dto.setAssignedAt(assignedAt);
        dto.setReleasedAt(releasedAt);
        return dto;
    }
}
