package kr.co.seoulit.his.inpatientservice.nursing.mapper;

import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RiskAssessmentHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.RiskAssessmentEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.RiskAssessmentHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RiskAssessmentMapper {
    RiskAssessmentDTO toDto(RiskAssessmentEntity entity);

    RiskAssessmentEntity toEntity(RiskAssessmentDTO dto);

    RiskAssessmentHistoryDTO toDto(RiskAssessmentHistoryEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(@MappingTarget RiskAssessmentEntity entity, RiskAssessmentDTO dto);
}
