package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedReservationDTO;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedReservationScheduleRequest;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationStatus;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedReservationMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedReservationHistoryRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedReservationRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.common.util.DateRules;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 병상예약 날짜 검증 — 입원 예정일은 오늘 ~ 30일 후, 예약일시는 미래 불가 · 입원 예정일보다 늦을 수 없음
class BedReservationDateTest {

    private final BedReservationRepository bedReservationRepository = mock(BedReservationRepository.class);
    private final BedRepository bedRepository = mock(BedRepository.class);
    private final BedReservationMapper bedReservationMapper = mock(BedReservationMapper.class);
    private final BedReservationServiceImpl service = new BedReservationServiceImpl(
            bedReservationRepository, bedReservationMapper, bedRepository, mock(BedReservationHistoryRepository.class));

    private static final LocalDate TODAY = DateRules.today();
    private static final LocalDateTime NOW = DateRules.now();

    @Test
    void 입원_예정일이_30일을_넘으면_거절된다() {
        assertRejected(() -> service.createBedReservation(request(NOW, TODAY.plusDays(31).atTime(10, 0))),
                ErrorCode.BED_RESERVATION_DATE_INVALID);
        assertRejected(() -> service.createBedReservation(request(NOW, TODAY.plusYears(1).atTime(10, 0))),
                ErrorCode.BED_RESERVATION_DATE_INVALID);
        verify(bedRepository, never()).findById(any());
    }

    @Test
    void 입원_예정일이_오늘_이전이면_거절된다() {
        assertRejected(() -> service.createBedReservation(request(NOW.minusDays(2), TODAY.minusDays(1).atTime(10, 0))),
                ErrorCode.BED_RESERVATION_DATE_INVALID);
    }

    @Test
    void 예약일시가_미래이거나_입원_예정일보다_늦으면_거절된다() {
        assertRejected(() -> service.createBedReservation(request(NOW.plusHours(1), TODAY.plusDays(3).atTime(10, 0))),
                ErrorCode.BED_RESERVATION_DATE_INVALID);
        // 입원 예정은 오늘 00:00인데 예약은 그보다 늦은 00:30에 함
        assertRejected(() -> service.createBedReservation(request(TODAY.atTime(0, 30), TODAY.atStartOfDay())),
                ErrorCode.BED_RESERVATION_DATE_INVALID);
    }

    @Test
    void 입원_예정일이_30일_후면_날짜_검증을_통과한다() {
        // 날짜 검증 통과 후 병상 조회 단계에서 병상이 없어 BED_NOT_FOUND
        assertRejected(() -> service.createBedReservation(request(NOW, TODAY.plusDays(30).atTime(23, 0))),
                ErrorCode.BED_NOT_FOUND);
    }

    @Test
    void 일정_변경_PATCH도_같은_규칙으로_검증한다() {
        when(bedReservationRepository.findById(1L)).thenReturn(Optional.of(existing(TODAY.plusDays(1).atTime(10, 0))));
        BedReservationScheduleRequest schedule = new BedReservationScheduleRequest(NOW, TODAY.plusDays(60).atTime(10, 0));
        assertRejected(() -> service.updateBedReservationSchedule(1L, schedule), ErrorCode.BED_RESERVATION_DATE_INVALID);
        verify(bedReservationRepository, never()).save(any());
    }

    @Test
    void 일정은_그대로_두고_상태만_바꾸는_수정은_지난_예약도_허용한다() {
        // 입원 예정일이 이미 지난 예약을 취소(RELEASED)하는 경우 — 날짜를 바꾸지 않았으므로 검증하지 않음
        LocalDateTime pastExpected = TODAY.minusDays(5).atTime(10, 0);
        BedReservationEntity entity = existing(pastExpected);
        when(bedReservationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(bedReservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BedReservationDTO cancel = request(entity.getReserveAt(), pastExpected);
        cancel.setReservationStatusCd(BedReservationStatus.RELEASED);

        // 날짜 검증을 통과하면 병상 EMPTY 처리 단계에서 병상이 없어 BED_NOT_FOUND
        assertRejected(() -> service.updateBedReservation(1L, cancel), ErrorCode.BED_NOT_FOUND);
    }

    private BedReservationEntity existing(LocalDateTime expectedAdmissionAt) {
        return BedReservationEntity.builder()
                .bedReservationId(1L).bedId("BED001").patientId("P1")
                .reserveAt(expectedAdmissionAt.minusDays(1)).expectedAdmissionAt(expectedAdmissionAt)
                .reservationStatusCd(BedReservationStatus.REQUESTED).build();
    }

    private void assertRejected(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(expected);
    }

    private BedReservationDTO request(LocalDateTime reserveAt, LocalDateTime expectedAdmissionAt) {
        BedReservationDTO dto = new BedReservationDTO();
        dto.setBedId("BED001");
        dto.setPatientId("P1");
        dto.setReserveAt(reserveAt);
        dto.setExpectedAdmissionAt(expectedAdmissionAt);
        return dto;
    }
}
