package kr.co.seoulit.his.inpatientservice.admission.service;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;

import java.util.List;

public interface AdmissionService {
    List<AdmissionDTO> getAdmissions();

    AdmissionDTO createAdmission(AdmissionDTO requestDto);

    AdmissionDTO getAdmission(String admissionId);

    AdmissionDTO updateAdmission(String admissionId, AdmissionDTO requestDto);

}
