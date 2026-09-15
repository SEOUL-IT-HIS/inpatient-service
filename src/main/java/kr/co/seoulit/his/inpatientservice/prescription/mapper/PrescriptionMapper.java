package kr.co.seoulit.his.inpatientservice.prescription.mapper;

import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionDTO;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionItemDTO;
import kr.co.seoulit.his.inpatientservice.prescription.entity.PrescriptionEntity;
import kr.co.seoulit.his.inpatientservice.prescription.entity.PrescriptionItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {

    @Mapping(target = "items", ignore = true)
    PrescriptionDTO toDto(PrescriptionEntity entity);

    PrescriptionEntity toEntity(PrescriptionDTO dto);

    PrescriptionItemDTO toItemDto(PrescriptionItemEntity entity);

    PrescriptionItemEntity toItemEntity(PrescriptionItemDTO dto);
}
