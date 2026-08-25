package kr.co.seoulit.his.inpatientservice.nursing.service;


import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.RiskAssessmentEntity;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.RiskAssessmentMapper;
import kr.co.seoulit.his.inpatientservice.nursing.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiskAssessmentServiceImpl implements RiskAssessmentService {
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RiskAssessmentMapper riskAssessmentMapper;


    @Override
    public List<RiskAssessmentDTO> getRiskAssessments() {
        return riskAssessmentRepository.findAll().stream()
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


    @Override
    public RiskAssessmentDTO updateRiskAssessment(String riskAssessmentId, RiskAssessmentDTO requestDto) {
        return riskAssessmentRepository.findById(riskAssessmentId)
                .map(entity -> {
                    riskAssessmentMapper.updateEntityFromDto(entity, requestDto);
                    return riskAssessmentMapper.toDto(riskAssessmentRepository.save(entity));

                })
                .orElseThrow(() -> new RuntimeException("Risk assessment not found with ID: " + riskAssessmentId));
    }
    @Override
    public void deleteRiskAssessment(String riskAssessmentId) {
        // Implementation for deleting a specific risk assessment
        RiskAssessmentEntity entity = riskAssessmentRepository.findById(riskAssessmentId)
                .orElseThrow(()->new RuntimeException("Risk assessment not found with ID: " + riskAssessmentId));
        riskAssessmentRepository.delete(entity);
    }
}
