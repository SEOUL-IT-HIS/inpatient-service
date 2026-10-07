package kr.co.seoulit.his.inpatientservice.nursing.service;

import jakarta.transaction.Transactional;
import kr.co.seoulit.his.inpatientservice.common.aop.HistoryTrackable;
import kr.co.seoulit.his.inpatientservice.common.aop.TracksHistory;
import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.NursingAssessmentEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.NursingAssessmentHistoryEntity;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.NursingAssessmentMapper;
import kr.co.seoulit.his.inpatientservice.nursing.repository.NursingAssessmentHistoryRepository;
import kr.co.seoulit.his.inpatientservice.nursing.repository.NursingAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NursingAssessmentServiceImpl implements NursingAssessmentService, HistoryTrackable {
    private final NursingAssessmentRepository nursingAssessmentRepository;
    private final NursingRecordValidator nursingRecordValidator;
    private final NursingAssessmentMapper nursingAssessmentMapper;
    private final NursingAssessmentHistoryRepository nursingAssessmentHistoryRepository;

    @Override
    public void recordHistory(String id, String changeType) {
        NursingAssessmentEntity entity = nursingAssessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NursingAssessment not found with ID: " + id));
        nursingAssessmentHistoryRepository.save(toHistorySnapshot(entity, changeType));
    }
    @Override
    public List<NursingAssessmentDTO> getNursingAssessments() {
        return nursingAssessmentRepository.findAll().stream()
                .map(nursingAssessmentMapper::toDto)
                .toList();
    }

    @Override
    public List<NursingAssessmentHistoryDTO> getNursingAssessmentHistory(String nursingAssessmentId) {
        return nursingAssessmentHistoryRepository.findByNursingAssessmentIdOrderByChangedAtDesc(nursingAssessmentId).stream()
                .map(nursingAssessmentMapper::toDto)
                .toList();
    }


    @Override
    public NursingAssessmentDTO createNursingAssessment(NursingAssessmentDTO requestDto) {
        nursingRecordValidator.validateWritable(requestDto.getAdmissionId());
        nursingRecordValidator.validateRecordTime(requestDto.getAdmissionId(), requestDto.getAssessedAt());
        NursingAssessmentEntity entity = nursingAssessmentMapper.toEntity(requestDto);
        entity.setNursingAssessmentId(UUID.randomUUID().toString());
        NursingAssessmentDTO savedDto = nursingAssessmentMapper.toDto(nursingAssessmentRepository.save(entity));
        return savedDto;

    }

    @Override
    public NursingAssessmentDTO getNursingAssessment(String nursingAssessmentId) {
        // Implementation for retrieving a specific nursing assessment
        NursingAssessmentDTO nursingAssessmentDTO = nursingAssessmentRepository.findById(nursingAssessmentId)
                .map(nursingAssessmentMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Nursing assessment not found with ID: " + nursingAssessmentId));
        return nursingAssessmentDTO;
    }


    // 변경이력은 HistoryTrackingAspect가 실행 직전에 저장 (어노테이션은 구현 메서드에 있어야 AOP가 적용됨)
    // @Transactional: AOP가 남기는 이력과 실제 수정을 하나로 묶음 → 검증 실패 등으로 수정이 취소되면 이력도 남지 않음
    @Transactional
    @TracksHistory(changeType = "UPDATED")
    @Override
    public NursingAssessmentDTO updateNursingAssessment(String nursingAssessmentId, NursingAssessmentDTO requestDto) {
        return nursingAssessmentRepository.findById(nursingAssessmentId)
                .map(entity -> {
                    nursingRecordValidator.validateWritable(entity.getAdmissionId());
                    // 기록 시각을 보낸 경우만 검증 (비어 있으면 기존 값 유지)
                    if (requestDto.getAssessedAt() != null) {
                        nursingRecordValidator.validateRecordTime(entity.getAdmissionId(), requestDto.getAssessedAt());
                    }
                    nursingAssessmentMapper.updateEntityFromDto(entity, requestDto);
                    return nursingAssessmentMapper.toDto(nursingAssessmentRepository.save(entity));
                })
                .orElseThrow(() -> new RuntimeException("Nursing assessment not found with ID: " + nursingAssessmentId));
    }
    @Transactional
    @TracksHistory(changeType = "DELETED")
    @Override
    public void deleteNursingAssessment(String nursingAssessmentId) {
        // Implementation for deleting a specific nursing assessment
        NursingAssessmentEntity entity = nursingAssessmentRepository.findById(nursingAssessmentId)
                .orElseThrow(()->new RuntimeException("Nursing assessment not found with ID: " + nursingAssessmentId));
        nursingRecordValidator.validateWritable(entity.getAdmissionId());
        nursingAssessmentRepository.delete(entity);
    }

    private NursingAssessmentHistoryEntity toHistorySnapshot(NursingAssessmentEntity entity, String changeType) {
        return NursingAssessmentHistoryEntity.builder()
                .nursingAssessmentId(entity.getNursingAssessmentId())
                .admissionId(entity.getAdmissionId())
                .allergyYn(entity.getAllergyYn())
                .allergyDetail(entity.getAllergyDetail())
                .pastMedicalHistory(entity.getPastMedicalHistory())
                .mentalStatusCd(entity.getMentalStatusCd())
                .assessedAt(entity.getAssessedAt())
                .assessorId(entity.getAssessorId())
                .changeType(changeType)
                .build();
    }
}
