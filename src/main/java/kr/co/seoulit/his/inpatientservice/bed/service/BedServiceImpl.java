package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        return bedMapper.toDto(entity);
    }

    @Override
    public BedDTO updateRoomType(String bedId, String roomTypeCode){
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(()->new BusinessException(ErrorCode.BED_NOT_FOUND));
        entity.setRoomTypeCode(roomTypeCode);
        BedEntity saved = bedRepository.save(entity);
        return bedMapper.toDto(saved);
    }

    @Override
    public BedDTO updateWard(String bedId, String wardCd){
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(()->new BusinessException(ErrorCode.BED_NOT_FOUND));
        entity.setWardCd(wardCd);
        BedEntity saved = bedRepository.save(entity);
        return bedMapper.toDto(saved);
    }

    // 청소 완료: 퇴상 후 점검중(MAINTENANCE)이 된 병상을 다시 배정할 수 있는 EMPTY로 되돌림
    // 점검중이 아닌 병상(사용 중·예약·이미 빈 병상)에 호출하면 거절 — 환자가 있는 병상을 실수로 비우지 않도록
    @Override
    public BedDTO makeBedAvailable(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        if (entity.getBedStatus() != BedStatus.MAINTENANCE) {
            throw new BusinessException(ErrorCode.BED_NOT_IN_MAINTENANCE);
        }
        entity.setBedStatus(BedStatus.EMPTY);
        entity.setPatientId(null);
        return bedMapper.toDto(bedRepository.save(entity));
    }
}
