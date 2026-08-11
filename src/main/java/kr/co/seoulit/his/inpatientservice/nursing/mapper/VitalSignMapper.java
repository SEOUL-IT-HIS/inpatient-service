package kr.co.seoulit.his.inpatientservice.nursing.mapper;

import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.VitalSignEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VitalSignMapper {
    VitalSignDTO toDto(VitalSignEntity entity);

    VitalSignEntity toEntity(VitalSignDTO dto);

    void updateEntityFromDto(@MappingTarget VitalSignEntity entity, VitalSignDTO dto);
}
