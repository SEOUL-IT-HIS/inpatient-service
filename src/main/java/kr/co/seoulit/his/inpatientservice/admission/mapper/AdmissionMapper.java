package kr.co.seoulit.his.inpatientservice.admission.mapper;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdmissionMapper {
    AdmissionDTO toDto(AdmissionEntity entity);

    AdmissionEntity toEntity(AdmissionDTO dto);
}
