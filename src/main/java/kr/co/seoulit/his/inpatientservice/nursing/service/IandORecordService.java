package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.common.aop.TracksHistory;
import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordHistoryDTO;

import java.util.List;

public interface IandORecordService {
    List<IandORecordDTO> getIandORecords();

    List<IandORecordHistoryDTO> getIandORecordHistory(String intakeOutputId);

    IandORecordDTO createIandORecord(IandORecordDTO requestDto);

    IandORecordDTO getIandORecord(String IandORecordId);

    @TracksHistory(changeType = "UPDATED")
    IandORecordDTO updateIandORecord(String IandORecordId, IandORecordDTO requestDto);

    @TracksHistory(changeType = "DELETED")
    void deleteIandORecord(String IandORecordId);
}
