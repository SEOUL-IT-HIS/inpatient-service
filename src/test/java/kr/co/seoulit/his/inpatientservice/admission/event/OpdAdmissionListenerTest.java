package kr.co.seoulit.his.inpatientservice.admission.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.admission.service.AdmissionService;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;

// 외래 입원요청 수신 — 브로커 없이 리스너 로직만 확인 (회신은 publisher mock으로 검증)
class OpdAdmissionListenerTest {

    private static final String ROUTE = OpdAdmissionRequestedEvent.ADMISSION_ROUTE;

    private final AdmissionService admissionService = mock(AdmissionService.class);
    private final AdmissionRepository admissionRepository = mock(AdmissionRepository.class);
    private final OpdAdmissionResultPublisher publisher = mock(OpdAdmissionResultPublisher.class);
    private final OpdAdmissionListener listener =
            new OpdAdmissionListener(admissionService, admissionRepository, publisher);

    private OpdAdmissionRequestedEvent request(String requestId, String patientId, String dept, String occurredAt,
                                               String preferred, String note) {
        return new OpdAdmissionRequestedEvent("e1", "AdmissionRequested", "1", occurredAt, "01",
                new OpdAdmissionRequestedEvent.Data(requestId, "ENC1", patientId, "D1", dept, "W1", preferred, note));
    }

    @Test
    void 정상_요청이면_외래_경로의_입원_건을_만들고_회신은_보내지_않는다() {
        when(admissionService.createAdmission(any())).thenReturn(AdmissionDTO.builder().admissionId("A001").build());

        listener.onAdmissionRequested(request("R1", "PAT1", "D01", "2026-10-01T09:00:00+09:00", "2026-10-05", "memo"));

        ArgumentCaptor<AdmissionDTO> captor = ArgumentCaptor.forClass(AdmissionDTO.class);
        verify(admissionService).createAdmission(captor.capture());
        AdmissionDTO dto = captor.getValue();
        assertEquals("Outpatient", dto.getAdmissionRoute());
        assertEquals("REQUESTED", dto.getStatus());
        assertEquals("R1", dto.getAdmissionRequestId());
        assertNull(dto.getDispositionId());
        assertEquals("PAT1", dto.getPatientId());
        assertEquals("D1", dto.getDoctorId());
        assertEquals("D1", dto.getRequestedBy());
        assertEquals("D01", dto.getAdmissionDeptId());
        assertEquals("W1", dto.getWardPref());
        assertEquals(LocalDate.of(2026, 10, 5), dto.getPreferredAdmitDate());
        assertEquals(java.time.LocalDateTime.of(2026, 10, 1, 9, 0), dto.getAdmissionDate());
        verify(publisher, never()).rejected(anyString(), anyString());
    }

    @Test
    void 오프셋이_없는_시각도_그대로_읽는다() {
        when(admissionService.createAdmission(any())).thenReturn(AdmissionDTO.builder().admissionId("A001").build());

        listener.onAdmissionRequested(request("R1", "PAT1", "D01", "2026-10-01T09:00:00", null, null));

        ArgumentCaptor<AdmissionDTO> captor = ArgumentCaptor.forClass(AdmissionDTO.class);
        verify(admissionService).createAdmission(captor.capture());
        assertEquals(java.time.LocalDateTime.of(2026, 10, 1, 9, 0), captor.getValue().getAdmissionDate());
        assertNull(captor.getValue().getPreferredAdmitDate());
    }

    @Test
    void requestId가_없으면_무시한다() {
        listener.onAdmissionRequested(request(" ", "PAT1", "D01", null, null, null));

        verify(admissionService, never()).createAdmission(any());
        verify(publisher, never()).rejected(any(), any());
    }

    @Test
    void 같은_requestId가_다시_오면_무시한다() {
        when(admissionRepository.existsByAdmissionRequestIdAndAdmissionRoute("R1", ROUTE)).thenReturn(true);

        listener.onAdmissionRequested(request("R1", "PAT1", "D01", null, null, null));

        verify(admissionService, never()).createAdmission(any());
        verify(publisher, never()).rejected(any(), any());
    }

    @Test
    void 필수값이_없으면_거부_회신한다() {
        listener.onAdmissionRequested(request("R1", null, "D01", null, null, null));
        verify(publisher).rejected("R1", "patientId is required");

        listener.onAdmissionRequested(request("R2", "PAT1", "", null, null, null));
        verify(publisher).rejected("R2", "targetDeptCode is required");

        listener.onAdmissionRequested(request("R3", "PAT1", "D01", null, null, "x".repeat(501)));
        verify(publisher).rejected("R3", "note must be 500 characters or less");

        verify(admissionService, never()).createAdmission(any());
    }

    @Test
    void 날짜_형식이_틀리면_거부_회신한다() {
        listener.onAdmissionRequested(request("R1", "PAT1", "D01", "not-a-time", null, null));
        listener.onAdmissionRequested(request("R2", "PAT1", "D01", null, "2026/10/05", null));

        verify(publisher).rejected(eq("R1"), anyString());
        verify(publisher).rejected(eq("R2"), anyString());
        verify(admissionService, never()).createAdmission(any());
    }

    @Test
    void 배정_대기_중인_입원_건이_있는_환자의_새_요청도_같은_사유로_거부한다() {
        // 병동의 중복 입원 검사(existsActiveAdmission)는 대기(REQUESTED)·입원 중 모두 진행중으로 봄 — createAdmission에서 예외로 올라옴
        when(admissionService.createAdmission(any()))
                .thenThrow(new BusinessException(ErrorCode.ADMISSION_ALREADY_ACTIVE));

        listener.onAdmissionRequested(request("R2", "PAT1", "D01", null, null, null));

        verify(publisher).rejected("R2", "Patient already has an active admission");
        verify(publisher, never()).bedAssignedAfterCommit(any(), any(), any());
    }

    @Test
    void 이미_입원_중인_환자면_사유와_함께_거부_회신한다() {
        when(admissionService.createAdmission(any()))
                .thenThrow(new BusinessException(ErrorCode.ADMISSION_ALREADY_ACTIVE));

        listener.onAdmissionRequested(request("R1", "PAT1", "D01", null, null, null));

        verify(publisher).rejected("R1", "Patient already has an active admission");
    }
}
