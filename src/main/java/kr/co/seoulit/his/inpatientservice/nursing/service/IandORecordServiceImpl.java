package kr.co.seoulit.his.inpatientservice.nursing.service;


import kr.co.seoulit.his.inpatientservice.common.aop.HistoryTrackable;
import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.IandORecordEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.IandORecordHistoryEntity;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.IandORecordMapper;
import kr.co.seoulit.his.inpatientservice.nursing.repository.IandORecordHistoryRepository;
import kr.co.seoulit.his.inpatientservice.nursing.repository.IandORecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IandORecordServiceImpl implements IandORecordService, HistoryTrackable {
    private final IandORecordRepository iandORecordRepository;
    private final IandORecordMapper iandORecordMapper;
    private final IandORecordHistoryRepository iandORecordHistoryRepository;

    @Override
    public void recordHistory(String id, String changeType) {
        IandORecordEntity entity = iandORecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IandORecord not found with ID: " + id));
        iandORecordHistoryRepository.save(toHistorySnapshot(entity, changeType));
    }

    @Override
    public List<IandORecordDTO> getIandORecords() {
        return iandORecordRepository.findAll().stream()
                .map(iandORecordMapper::toDto)
                .toList();
    }

    @Override
    public List<IandORecordHistoryDTO> getIandORecordHistory(String intakeOutputId) {
        return iandORecordHistoryRepository.findByIntakeOutputIdOrderByChangedAtDesc(intakeOutputId).stream()
                .map(iandORecordMapper::toDto)
                .toList();
    }


    @Override
    public IandORecordDTO createIandORecord(IandORecordDTO requestDto) {
        IandORecordEntity entity = iandORecordMapper.toEntity(requestDto);
        entity.setIntakeOutputId(UUID.randomUUID().toString());
        IandORecordDTO savedDto = iandORecordMapper.toDto(iandORecordRepository.save(entity));
        return savedDto;

    }

    @Override
    public IandORecordDTO getIandORecord(String iandORecordId) {
        // Implementation for retrieving a specific I and O record
        IandORecordDTO iandORecordDTO = iandORecordRepository.findById(iandORecordId)
                .map(iandORecordMapper::toDto)
                .orElseThrow(() -> new RuntimeException("I and O record not found with ID: " + iandORecordId));
        return iandORecordDTO;
    }


    @Override
    public IandORecordDTO updateIandORecord(String iandORecordId, IandORecordDTO requestDto) {
        return iandORecordRepository.findById(iandORecordId)
                .map(entity -> {
                    iandORecordHistoryRepository.save(toHistorySnapshot(entity, "UPDATED"));
                    iandORecordMapper.updateEntityFromDto(entity, requestDto);
                    return iandORecordMapper.toDto(iandORecordRepository.save(entity));

                })
                .orElseThrow(() -> new RuntimeException("I and O record not found with ID: " + iandORecordId));
    }
    @Override
    public void deleteIandORecord(String iandORecordId) {
        // Implementation for deleting a specific I and O record
        IandORecordEntity entity = iandORecordRepository.findById(iandORecordId)
                .orElseThrow(()->new RuntimeException("I and O record not found with ID: " + iandORecordId));
        iandORecordHistoryRepository.save(toHistorySnapshot(entity, "DELETED"));
        iandORecordRepository.delete(entity);
    }

    private IandORecordHistoryEntity toHistorySnapshot(IandORecordEntity entity, String changeType) {
        return IandORecordHistoryEntity.builder()
                .intakeOutputId(entity.getIntakeOutputId())
                .admissionId(entity.getAdmissionId())
                .recordedAt(entity.getRecordedAt())
                .ioTypeCd(entity.getIoTypeCd())
                .routeCd(entity.getRouteCd())
                .amountMl(entity.getAmountMl())
                .recorderId(entity.getRecorderId())
                .changeType(changeType)
                .build();
    }
}
