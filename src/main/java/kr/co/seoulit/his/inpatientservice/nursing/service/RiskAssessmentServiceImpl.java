package kr.co.seoulit.his.inpatientservice.nursing.service;

import jakarta.transaction.Transactional;

import kr.co.seoulit.his.inpatientservice.common.aop.HistoryTrackable;
import kr.co.seoulit.his.inpatientservice.common.aop.TracksHistory;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.RiskAssessmentEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.RiskAssessmentHistoryEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.VitalSignEntity;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.RiskAssessmentMapper;
import kr.co.seoulit.his.inpatientservice.nursing.repository.RiskAssessmentHistoryRepository;
import kr.co.seoulit.his.inpatientservice.nursing.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiskAssessmentServiceImpl implements RiskAssessmentService, HistoryTrackable {
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final NursingRecordValidator nursingRecordValidator;
    private final RiskAssessmentMapper riskAssessmentMapper;
    private final RiskAssessmentHistoryRepository riskAssessmentHistoryRepository;

    @Override
    public void recordHistory(String id,String changeType){
        RiskAssessmentEntity entity = riskAssessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RiskAssessment not found with ID: "+id));
        riskAssessmentHistoryRepository.save(toHistorySnapshot(entity, changeType));
    }

    @Override
    public List<RiskAssessmentDTO> getRiskAssessments() {
        return riskAssessmentRepository.findAll().stream()
                .map(riskAssessmentMapper::toDto)
                .toList();
    }

    @Override
    public List<RiskAssessmentHistoryDTO> getRiskAssessmentHistory(String patientRiskAssessmentId) {
        return riskAssessmentHistoryRepository.findByPatientRiskAssessmentIdOrderByChangedAtDesc(patientRiskAssessmentId).stream()
                .map(riskAssessmentMapper::toDto)
                .toList();
    }


    @Override
    public RiskAssessmentDTO createRiskAssessment(RiskAssessmentDTO requestDto) {
        nursingRecordValidator.validateWritable(requestDto.getAdmissionId());
        nursingRecordValidator.validateRecordTime(requestDto.getAdmissionId(), requestDto.getAssessedAt());
        RiskAssessmentEntity entity = riskAssessmentMapper.toEntity(requestDto);
        entity.setPatientRiskAssessmentId(UUID.randomUUID().toString());
        RiskAssessmentDTO savedDto = riskAssessmentMapper.toDto(riskAssessmentRepository.save(entity));
        return savedDto;

    }

    @Override
    public RiskAssessmentDTO getRiskAssessment(String riskAssessmentId) {
        // Implementation for retrieving a specific risk assessment
        RiskAssessmentDTO riskAssessmentDTO = riskAssessmentRepository.findById(riskAssessmentId)
                .map(riskAssessmentMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Risk assessment not found with ID: " + riskAssessmentId));
        return riskAssessmentDTO;
    }


    // 변경이력은 HistoryTrackingAspect가 실행 직전에 저장 (어노테이션은 구현 메서드에 있어야 AOP가 적용됨)
    // @Transactional: AOP가 남기는 이력과 실제 수정을 하나로 묶음 → 검증 실패 등으로 수정이 취소되면 이력도 남지 않음
    @Transactional
    @TracksHistory(changeType = "UPDATED")
    @Override
    public RiskAssessmentDTO updateRiskAssessment(String riskAssessmentId, RiskAssessmentDTO requestDto) {
        return riskAssessmentRepository.findById(riskAssessmentId)
                .map(entity -> {
                    nursingRecordValidator.validateWritable(entity.getAdmissionId());
                    // 기록 시각을 보낸 경우만 검증 (비어 있으면 기존 값 유지)
                    if (requestDto.getAssessedAt() != null) {
                        nursingRecordValidator.validateRecordTime(entity.getAdmissionId(), requestDto.getAssessedAt());
                    }
                    riskAssessmentMapper.updateEntityFromDto(entity, requestDto);
                    return riskAssessmentMapper.toDto(riskAssessmentRepository.save(entity));

                })
                .orElseThrow(() -> new RuntimeException("Risk assessment not found with ID: " + riskAssessmentId));
    }
    @Transactional
    @TracksHistory(changeType = "DELETED")
    @Override
    public void deleteRiskAssessment(String riskAssessmentId) {
        // Implementation for deleting a specific risk assessment
        RiskAssessmentEntity entity = riskAssessmentRepository.findById(riskAssessmentId)
                .orElseThrow(()->new RuntimeException("Risk assessment not found with ID: " + riskAssessmentId));
        nursingRecordValidator.validateWritable(entity.getAdmissionId());
        riskAssessmentRepository.delete(entity);
    }

    private RiskAssessmentHistoryEntity toHistorySnapshot(RiskAssessmentEntity entity, String changeType) {
        return RiskAssessmentHistoryEntity.builder()
                .patientRiskAssessmentId(entity.getPatientRiskAssessmentId())
                .admissionId(entity.getAdmissionId())
                .assessmentTypeCd(entity.getAssessmentTypeCd())
                .score(entity.getScore())
                .riskLevelCd(entity.getRiskLevelCd())
                .assessedAt(entity.getAssessedAt())
                .assessorId(entity.getAssessorId())
                .changeType(changeType)
                .build();
    }
}
