package kr.co.seoulit.his.inpatientservice.admission.service;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;

import java.util.List;

public interface AdmissionService {
    // [사용 중지] 호출하는 곳 없음 — AdmissionController의 /reception 주석 참고
    // AdmissionDTO receiveAdmission(AdmissionDTO requestDto);

    List<AdmissionDTO> getAdmissions();

    AdmissionDTO createAdmission(AdmissionDTO requestDto);

    AdmissionDTO getAdmission(String admissionId);

    AdmissionDTO updateAdmission(String admissionId, AdmissionDTO requestDto);

    AdmissionDTO changeStatus(String admissionId, String status);

    // 담당의(주치의) 지정/변경 — 응급 요청에 의사가 없거나, 병동이 주치의를 따로 정할 때
    AdmissionDTO changeDoctor(String admissionId, String doctorId);
}
