package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignHistoryDTO;

import java.util.List;

public interface VitalSignService {
    /** admissionId가 있으면 그 입원 건의 기록만, 없으면 전체 */
    List<VitalSignDTO> getVitalSigns(String admissionId);

    List<VitalSignHistoryDTO> getVitalSignHistory(String vitalSignId);

    VitalSignDTO createVitalSign(VitalSignDTO requestDto);

    VitalSignDTO getVitalSign(String vitalSignId);

    VitalSignDTO updateVitalSign(String vitalSignId, VitalSignDTO requestDto);

    void deleteVitalSign(String vitalSignId);
}
