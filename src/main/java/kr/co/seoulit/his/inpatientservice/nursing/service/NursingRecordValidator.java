package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.common.util.DateRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 간호기록 5종(활력징후 · I&O · 간호평가 · 위험도 · 억제대) 공통 검증
 * - 작성 가능 여부: 퇴원 완료(DISCHARGED)된 입원 건에는 등록 · 수정 · 삭제 불가 (조회만)
 *   퇴원신청(DISCHARGE_REQUESTED)은 정산 전까지 환자가 병동에 있으므로 계속 작성 가능
 * - 기록 시각: 이미 일어난 일의 기록이라 미래 시각 불가 (시계 오차 5분 여유),
 *   입원일 이전 불가 (날짜 기준 — 입원 당일 기록은 시각과 관계없이 허용)
 * - 입원 건을 못 찾거나 입원일이 비어 있으면 해당 검사는 건너뜀
 */
@Component
@RequiredArgsConstructor
public class NursingRecordValidator {

    private final AdmissionRepository admissionRepository;

    public void validateWritable(String admissionId) {
        if (admissionId == null) {
            return;
        }
        admissionRepository.findById(admissionId)
                .filter(admission -> "DISCHARGED".equals(admission.getStatus()))
                .ifPresent(admission -> {
                    throw new BusinessException(ErrorCode.ADMISSION_ALREADY_DISCHARGED);
                });
    }

    public void validateRecordTime(String admissionId, LocalDateTime recordTime) {
        if (recordTime == null || DateRules.isFuture(recordTime)) {
            throw new BusinessException(ErrorCode.NURSING_RECORD_TIME_INVALID);
        }
        if (admissionId == null) {
            return;
        }
        admissionRepository.findById(admissionId)
                .filter(admission -> admission.getAdmissionDate() != null)
                .filter(admission -> recordTime.toLocalDate().isBefore(admission.getAdmissionDate().toLocalDate()))
                .ifPresent(admission -> {
                    throw new BusinessException(ErrorCode.NURSING_RECORD_TIME_INVALID);
                });
    }
}
