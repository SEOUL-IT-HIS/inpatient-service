package kr.co.seoulit.his.inpatientservice.nursing.mapper;

import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.VitalSignEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VitalSignMapper {
    VitalSignDTO toDto(VitalSignEntity entity);

    VitalSignEntity toEntity(VitalSignDTO dto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(@MappingTarget VitalSignEntity entity, VitalSignDTO dto);
}
