package kr.co.seoulit.his.inpatientservice.bed.mapper;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BedAssignmentMapper {

    BedAssignmentDTO toDto(BedAssignmentEntity entity);

    BedAssignmentEntity toEntity(BedAssignmentDTO dto);
}
