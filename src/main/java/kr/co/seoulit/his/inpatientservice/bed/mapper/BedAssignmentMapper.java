package kr.co.seoulit.his.inpatientservice.bed.mapper;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentRequest;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentResponse;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BedAssignmentMapper {

    @Mapping(target = "assignmentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BedAssignment toEntity(BedAssignmentRequest request);

    BedAssignmentResponse toResponse(BedAssignment entity);
}
