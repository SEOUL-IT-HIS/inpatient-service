package kr.co.seoulit.his.inpatientservice.nursing.service;


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
    @TracksHistory(changeType = "UPDATED")
    @Override
    public RiskAssessmentDTO updateRiskAssessment(String riskAssessmentId, RiskAssessmentDTO requestDto) {
        return riskAssessmentRepository.findById(riskAssessmentId)
                .map(entity -> {
                    riskAssessmentMapper.updateEntityFromDto(entity, requestDto);
                    return riskAssessmentMapper.toDto(riskAssessmentRepository.save(entity));

                })
                .orElseThrow(() -> new RuntimeException("Risk assessment not found with ID: " + riskAssessmentId));
    }
    @TracksHistory(changeType = "DELETED")
    @Override
    public void deleteRiskAssessment(String riskAssessmentId) {
        // Implementation for deleting a specific risk assessment
        RiskAssessmentEntity entity = riskAssessmentRepository.findById(riskAssessmentId)
                .orElseThrow(()->new RuntimeException("Risk assessment not found with ID: " + riskAssessmentId));
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
