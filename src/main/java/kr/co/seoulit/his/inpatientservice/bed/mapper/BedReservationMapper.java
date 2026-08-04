package kr.co.seoulit.his.inpatientservice.bed.mapper;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedReservationDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BedReservationMapper {
    BedReservationDTO toDto(BedReservationEntity entity);

    BedReservationEntity toEntity(BedReservationDTO dto);
}
