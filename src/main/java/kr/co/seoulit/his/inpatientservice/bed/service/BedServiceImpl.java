package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class BedServiceImpl implements BedService {
    private final BedRepository bedRepository;
    private final BedMapper bedMapper;

    public BedServiceImpl(BedRepository bedRepository,
            BedMapper bedMapper) {
        this.bedRepository = bedRepository;
        this.bedMapper = bedMapper;
    }

    @Override
    public List<BedDTO> getBeds() {
        return bedRepository.findAll().stream()
                .map(bedMapper::toDto)
                .toList();
    }

    @Override
    public BedDTO getBed(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        return bedMapper.toDto(entity);
    }

    @Override
    public BedDTO updateRoomType(String bedId, String roomTypeCode) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        entity.setRoomTypeCode(roomTypeCode);
        return bedMapper.toDto(bedRepository.save(entity));
    }

}
