package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.common.aop.HistoryTrackable;
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


    @Override
    public NursingAssessmentDTO updateNursingAssessment(String nursingAssessmentId, NursingAssessmentDTO requestDto) {
        return nursingAssessmentRepository.findById(nursingAssessmentId)
                .map(entity -> {
                    nursingAssessmentHistoryRepository.save(toHistorySnapshot(entity, "UPDATED"));
                    nursingAssessmentMapper.updateEntityFromDto(entity, requestDto);
                    return nursingAssessmentMapper.toDto(nursingAssessmentRepository.save(entity));
                })
                .orElseThrow(() -> new RuntimeException("Nursing assessment not found with ID: " + nursingAssessmentId));
    }
    @Override
    public void deleteNursingAssessment(String nursingAssessmentId) {
        // Implementation for deleting a specific nursing assessment
        NursingAssessmentEntity entity = nursingAssessmentRepository.findById(nursingAssessmentId)
                .orElseThrow(()->new RuntimeException("Nursing assessment not found with ID: " + nursingAssessmentId));
        nursingAssessmentHistoryRepository.save(toHistorySnapshot(entity, "DELETED"));
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
