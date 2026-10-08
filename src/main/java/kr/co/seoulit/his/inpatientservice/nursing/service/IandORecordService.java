package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordHistoryDTO;

import java.util.List;

public interface IandORecordService {
    /** admissionId가 있으면 그 입원 건의 기록만, 없으면 전체 */
    List<IandORecordDTO> getIandORecords(String admissionId);

    List<IandORecordHistoryDTO> getIandORecordHistory(String intakeOutputId);

    IandORecordDTO createIandORecord(IandORecordDTO requestDto);

    IandORecordDTO getIandORecord(String IandORecordId);

    IandORecordDTO updateIandORecord(String IandORecordId, IandORecordDTO requestDto);

    void deleteIandORecord(String IandORecordId);
}
