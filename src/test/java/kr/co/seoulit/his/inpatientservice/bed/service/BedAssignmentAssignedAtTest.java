package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// 병상배정 등록 시 배정일시 검증 — 오늘 이전/빈 값은 DB를 건드리기 전에 거절
class BedAssignmentAssignedAtTest {

    private final BedAssignmentRepository bedAssignmentRepository = mock(BedAssignmentRepository.class);
    private final BedRepository bedRepository = mock(BedRepository.class);

    @SuppressWarnings("unchecked")
    private final BedAssignmentServiceImpl service = new BedAssignmentServiceImpl(
            bedAssignmentRepository, mock(BedAssignmentMapper.class), bedRepository,
            mock(BedReservationService.class), mock(AdmissionRepository.class),
            mock(KafkaTemplate.class), "inpatient.admission.bed-assigned.v1", false);

    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Seoul"));

    @Test
    void 어제_날짜로_배정하면_거절되고_DB는_건드리지_않는다() {
        assertThatThrownBy(() -> service.createBedAssignment(request(TODAY.minusDays(1).atTime(23, 59))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BED_ASSIGNED_AT_INVALID);

        verify(bedAssignmentRepository, never()).findByAdmissionIdAndReleasedAtIsNull(any());
        verify(bedAssignmentRepository, never()).save(any());
    }

    @Test
    void 배정일시가_비어_있으면_거절된다() {
        assertThatThrownBy(() -> service.createBedAssignment(request(null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BED_ASSIGNED_AT_INVALID);
    }

    @Test
    void 오늘_0시_배정은_날짜_검증을_통과한다() {
        // 날짜 검증을 통과하면 다음 단계(병상 조회)로 넘어감 — 병상이 없으니 BED_NOT_FOUND가 나와야 정상
        assertThatThrownBy(() -> service.createBedAssignment(request(TODAY.atStartOfDay())))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BED_NOT_FOUND);
        verify(bedAssignmentRepository).findByAdmissionIdAndReleasedAtIsNull("ADM001");
    }

    private BedAssignmentDTO request(LocalDateTime assignedAt) {
        BedAssignmentDTO dto = new BedAssignmentDTO();
        dto.setBedId("BED001");
        dto.setAdmissionId("ADM001");
        dto.setAssignedAt(assignedAt);
        assertThat(dto.getReleasedAt()).isNull();
        return dto;
    }
}
