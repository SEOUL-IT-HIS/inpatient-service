package kr.co.seoulit.his.inpatientservice.prescription.service;

import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionCreateDTO;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionDTO;

import java.util.List;

public interface PrescriptionService {
    PrescriptionDTO createPrescription(String admissionId, PrescriptionCreateDTO requestDto);

    List<PrescriptionDTO> getPrescriptionsByAdmission(String admissionId);

    PrescriptionDTO getPrescription(String prescriptionId);
}
