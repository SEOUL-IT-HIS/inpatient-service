package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.common.aop.TracksHistory;
import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignHistoryDTO;

import java.util.List;

public interface VitalSignService {
    List<VitalSignDTO> getVitalSigns();

    List<VitalSignHistoryDTO> getVitalSignHistory(String vitalSignId);

    VitalSignDTO createVitalSign(VitalSignDTO requestDto);

    VitalSignDTO getVitalSign(String vitalSignId);

    @TracksHistory(changeType = "UPDATED")
    VitalSignDTO updateVitalSign(String vitalSignId, VitalSignDTO requestDto);

    @TracksHistory(changeType = "DELETED")
    void deleteVitalSign(String vitalSignId);
}
