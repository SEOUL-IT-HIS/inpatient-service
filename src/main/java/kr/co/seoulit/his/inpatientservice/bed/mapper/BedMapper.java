package kr.co.seoulit.his.inpatientservice.bed.mapper;


import kr.co.seoulit.his.inpatientservice.bed.dto.BedDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BedMapper {
    BedDTO toDto(BedEntity entity);

    BedEntity toEntity(BedDTO dto);
}
