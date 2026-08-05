package kr.co.seoulit.his.inpatientservice.bed.service;

import jakarta.transaction.Transactional;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedReservationDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedReservationStatus;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedReservationMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedReservationRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BedReservationServiceImpl implements BedReservationService {
    // "예약(BedReservation)" 테이블을 다루는 repository — BED_RESERVATION 저장/조회 담당
    private final BedReservationRepository bedReservationRepository;
    // Entity <-> DTO 변환 도구
    private final BedReservationMapper bedReservationMapper;
    // "병상(Bed)" 테이블을 다루는 repository — 예약이 아니라 "병상 자체의 상태"를 바꿀 때 씀
    private final BedRepository bedRepository;

    public BedReservationServiceImpl(BedReservationRepository bedReservationRepository,
            BedReservationMapper bedReservationMapper, BedRepository bedRepository) {
        this.bedReservationRepository = bedReservationRepository;
        this.bedReservationMapper = bedReservationMapper;
        this.bedRepository = bedRepository;
    }

    //
    // [검증 전용 private 메서드] "이 병상, 지금 새로 예약해도 되는 상태냐?"만 확인 → createBedReservation에서
    // 사용
    private void validateBedAvailable(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));

        if (entity.getBedStatus() != BedStatus.EMPTY) {
            throw new BusinessException(ErrorCode.BED_NOT_AVAILABLE);
        }
    }

    // [조회] 예약 전체 목록 — 그냥 읽기만, 문제없음
    @Override
    public List<BedReservationDTO> getBedReservations() {
        return bedReservationRepository.findAll().stream()
                .map(bedReservationMapper::toDto)
                .toList();
    }

    // [수정] 기존 예약을 고침 — id로 찾아서 필드 덮어쓴 뒤 저장, RELEASED면 병상을 EMPTY로 되돌림
    @Transactional
    @Override

    public BedReservationDTO updateBedReservation(Long bedReservationId, BedReservationDTO requestDto) {
        // 1) id로 "기존" 예약을 찾음 → 이건 새로 만드는 게 아니라 이미 있는 걸 찾아서 고치는 동작
        BedReservationEntity entity = bedReservationRepository.findById(bedReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_RESERVATION_NOT_FOUND));

        // 2) 요청받은 값으로 필드 덮어쓰기 (메모리 상에서만, 아직 DB 반영 전)

        if (!entity.getBedId().equals(requestDto.getBedId())) {
            throw new BusinessException(ErrorCode.BED_NOT_AVAILABLE);
        }
        entity.setBedId(requestDto.getBedId());
        entity.setPatientId(requestDto.getPatientId());
        entity.setReservationStatusCd(requestDto.getReservationStatusCd());
        entity.setReserveAt(requestDto.getReserveAt());
        entity.setExpectedAdmissionAt(requestDto.getExpectedAdmissionAt());
        // 3) DB에 저장 (사실상 UPDATE 문이 나감 — id가 이미 있는 row라서)
        BedReservationEntity updated = bedReservationRepository.save(entity);

        // 4) 상태값이 "RELEASED"(취소/해제)면 그 병상을 다시 EMPTY로 되돌림
        // ⚠ 이 취소 로직도 사실 "수정" 과정에서 나오는 거라 update 메서드에 있는 게 맞음
        if (updated.getReservationStatusCd() == BedReservationStatus.RELEASED) {
            markBedEmpty(updated.getBedId());
        }
        return bedReservationMapper.toDto(updated);
    }

    // [조회] 예약 하나만 id로 가져오기 — 그냥 읽기만 하는 게 맞음, 이 메서드는 정상
    @Transactional
    @Override
    public BedReservationDTO getBedReservation(Long bedReservationId) {
        BedReservationEntity entity = bedReservationRepository.findById(bedReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_RESERVATION_NOT_FOUND));
        return bedReservationMapper.toDto(entity);
    }

    // [생성] 새 예약 만들기 = BedAssignmentServiceImpl.createBedAssignment와 동일한 순서
    // 1) 병상이 예약 가능한 상태인지 검증 → 2) 새 엔티티로 저장 → 3) 병상 상태를 RESERVED로 변경
    @Transactional
    @Override
    public BedReservationDTO createBedReservation(BedReservationDTO requestDto) {
        validateBedAvailable(requestDto.getBedId());
        if (hasActiveReservation(requestDto.getBedId())) {
            throw new BusinessException(ErrorCode.BED_RESERVATION_ALREADY_ACTIVE);
        }
        BedReservationEntity entity = bedReservationMapper.toEntity(requestDto);
        entity.setReservationStatusCd(BedReservationStatus.REQUESTED);
        BedReservationEntity saved = bedReservationRepository.save(entity);
        markBedReserved(saved.getBedId());
        return bedReservationMapper.toDto(saved);
    }

    // [삭제] 예약 레코드를 지우고, 아직 해제(RELEASED)되지 않은 예약이었다면 병상도 다시 EMPTY로 되돌림
    @Transactional
    @Override
    public void deleteBedReservation(Long bedReservationId) {
        BedReservationEntity entity = bedReservationRepository.findById(bedReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_RESERVATION_NOT_FOUND));
        bedReservationRepository.delete(entity);
        if (entity.getReservationStatusCd() != BedReservationStatus.RELEASED) {
            markBedEmpty(entity.getBedId());
        }
    }

    // [병상 상태 변경 전용] bedId로 병상을 찾아서 RESERVED로 바꿈 → createBedReservation에서 사용
    private void markBedReserved(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        entity.setBedStatus(BedStatus.RESERVED);
        bedRepository.save(entity);
    }

    //
    //
    // [병상 상태 변경 전용] bedId로 병상을 찾아서 EMPTY로 바꿈 → updateBedReservation,
    // deleteBedReservation에서 사용
    private void markBedEmpty(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        entity.setBedStatus(BedStatus.EMPTY);
        bedRepository.save(entity);
    }

    // [조회 전용] 이 병상에 아직 안 끝난(REQUESTED/RESERVED) 예약이 있는지 — BedAssignmentServiceImpl도
    // 이 메서드를 씀
    @Override
    public boolean hasActiveReservation(String bedId) {
        return bedReservationRepository.existsByBedIdAndReservationStatusCdIn(
                bedId, List.of(BedReservationStatus.REQUESTED, BedReservationStatus.RESERVED));
    }
}
