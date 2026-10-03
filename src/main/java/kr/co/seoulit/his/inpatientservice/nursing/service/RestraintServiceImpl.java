package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.common.aop.HistoryTrackable;
import kr.co.seoulit.his.inpatientservice.common.aop.TracksHistory;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.RestraintEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.RestraintHistoryEntity;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.RestraintMapper;
import kr.co.seoulit.his.inpatientservice.nursing.repository.RestraintHistoryRepository;
import kr.co.seoulit.his.inpatientservice.nursing.repository.RestraintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestraintServiceImpl implements RestraintService, HistoryTrackable {
    private final RestraintRepository restraintRepository;
    private final RestraintMapper restraintMapper;
    private final RestraintHistoryRepository restraintHistoryRepository;

    @Override
    public void recordHistory(String id, String changeType) {
        RestraintEntity entity = restraintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restraint not found with ID: " + id));
        restraintHistoryRepository.save(toHistorySnapshot(entity, changeType));
    }

    @Override
    public List<RestraintDTO> getRestraints() {
        return restraintRepository.findAll().stream()
                .map(restraintMapper::toDto)
                .toList();
    }

    @Override
    public List<RestraintHistoryDTO> getRestraintHistory(String restraintId) {
        return restraintHistoryRepository.findByRestraintIdOrderByChangedAtDesc(restraintId).stream()
                .map(restraintMapper::toDto)
                .toList();
    }


    @Override
    public RestraintDTO createRestraint(RestraintDTO requestDto) {
        RestraintEntity entity = restraintMapper.toEntity(requestDto);
        entity.setRestraintId(UUID.randomUUID().toString());
        RestraintDTO savedDto = restraintMapper.toDto(restraintRepository.save(entity));
        return savedDto;

    }

    @Override
    public RestraintDTO getRestraint(String restraintId) {
        // Implementation for retrieving a specific restraint
        RestraintDTO restraintDTO = restraintRepository.findById(restraintId)
                .map(restraintMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Restraint not found with ID: " + restraintId));
        return restraintDTO;
    }


    // 변경이력은 HistoryTrackingAspect가 실행 직전에 저장 (어노테이션은 구현 메서드에 있어야 AOP가 적용됨)
    @TracksHistory(changeType = "UPDATED")
    @Override
    public RestraintDTO updateRestraint(String restraintId, RestraintDTO requestDto) {
        return restraintRepository.findById(restraintId)
                .map(entity -> {
                    restraintMapper.updateEntityFromDto(entity, requestDto);
                    return restraintMapper.toDto(restraintRepository.save(entity));

                })
                .orElseThrow(() -> new RuntimeException("Restraint not found with ID: " + restraintId));
    }
    @TracksHistory(changeType = "DELETED")
    @Override
    public void deleteRestraint(String restraintId) {
        // Implementation for deleting a specific restraint
        RestraintEntity entity = restraintRepository.findById(restraintId)
                .orElseThrow(()->new RuntimeException("Restraint not found with ID: " + restraintId));
        restraintRepository.delete(entity);
    }

    private RestraintHistoryEntity toHistorySnapshot(RestraintEntity entity, String changeType) {
        return RestraintHistoryEntity.builder()
                .restraintId(entity.getRestraintId())
                .admissionId(entity.getAdmissionId())
                .restraintTypeCd(entity.getRestraintTypeCd())
                .appliedAt(entity.getAppliedAt())
                .reason(entity.getReason())
                .doctorOrderId(entity.getDoctorOrderId())
                .evaluatorId(entity.getEvaluatorId())
                .changeType(changeType)
                .build();
    }
}
