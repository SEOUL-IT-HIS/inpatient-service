package kr.co.seoulit.his.inpatientservice.nursing.mapper;


import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.NursingAssessmentHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.NursingAssessmentEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.NursingAssessmentHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NursingAssessmentMapper {
    NursingAssessmentDTO toDto(NursingAssessmentEntity entity);

    NursingAssessmentEntity toEntity(NursingAssessmentDTO dto);

    NursingAssessmentHistoryDTO toDto(NursingAssessmentHistoryEntity entity);
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(@MappingTarget NursingAssessmentEntity entity, NursingAssessmentDTO dto);
}
