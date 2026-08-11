package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignDTO;

import java.util.List;

public interface VitalSignService {
    List<VitalSignDTO> getVitalSigns();

    VitalSignDTO createVitalSign(VitalSignDTO requestDto);

    VitalSignDTO getVitalSign(String vitalSignId);

    VitalSignDTO updateVitalSign(String vitalSignId, VitalSignDTO requestDto);

    void deleteVitalSign(String vitalSignId);
}
