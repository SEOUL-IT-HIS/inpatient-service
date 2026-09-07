package kr.co.seoulit.his.inpatientservice.nursing.mapper;


import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.RestraintHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.RestraintEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.RestraintHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestraintMapper{
    RestraintDTO toDto(RestraintEntity entity);

    RestraintEntity toEntity(RestraintDTO dto);

    RestraintHistoryDTO toDto(RestraintHistoryEntity entity);
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(@MappingTarget RestraintEntity entity, RestraintDTO dto);
}
