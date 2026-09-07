package kr.co.seoulit.his.inpatientservice.nursing.mapper;

import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.IandORecordEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.IandORecordHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IandORecordMapper {
    IandORecordDTO toDto(IandORecordEntity entity);

    IandORecordEntity toEntity(IandORecordDTO dto);

    IandORecordHistoryDTO toDto(IandORecordHistoryEntity entity);
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(@MappingTarget IandORecordEntity entity, IandORecordDTO dto);
}
