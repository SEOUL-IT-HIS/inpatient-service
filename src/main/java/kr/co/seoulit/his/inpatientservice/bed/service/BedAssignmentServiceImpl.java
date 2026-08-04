package kr.co.seoulit.his.inpatientservice.bed.service;

import jakarta.transaction.Transactional;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignmentEntity;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;

import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class BedAssignmentServiceImpl implements BedAssignmentService {
    // "배정(BedAssignment)" 테이블을 다루는 repository — BED_ASSIGNMENT 저장/조회 담당
    private final BedAssignmentRepository bedAssignmentRepository;
    // Entity(DB용 객체) <-> DTO(API 응답용 객체) 서로 변환해주는 도구
    private final BedAssignmentMapper bedAssignmentMapper;
    // "병상(Bed)" 테이블을 다루는 repository — BED 저장/조회 담당. 배정이 아니라 "병상 자체의 상태"를 바꿀 때 씀
    private final BedRepository bedRepository;

    // 생성자 — 스프링이 위 3개(repository 2개 + mapper 1개)를 자동으로 넣어줌(의존성 주입)
    public BedAssignmentServiceImpl(BedAssignmentRepository bedAssignmentRepository,
            BedAssignmentMapper bedAssignmentMapper,
            BedRepository bedRepository) {
        this.bedAssignmentRepository = bedAssignmentRepository;
        this.bedAssignmentMapper = bedAssignmentMapper;
        this.bedRepository = bedRepository;
    }

    // [조회] 배정 전체 목록 가져오기 — 아무것도 바꾸지 않음, 그냥 읽기만
    @Override
    public List<BedAssignmentDTO> getBedAssignments() {
        return bedAssignmentRepository.findAll().stream() // DB에서 전체 배정 row 가져옴 (Entity 리스트)
                .map(bedAssignmentMapper::toDto)           // 각 Entity를 DTO로 변환 (화면/API에 내려줄 형태)
                .toList();                                  // 다시 List로 모음
    }

    // [조회] 배정 하나만 id로 가져오기 — 이것도 그냥 읽기만, 상태 변경 없음
    @Override
    public BedAssignmentDTO getBedAssignment(Long assignmentId) {
        BedAssignmentEntity entity = bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND)); // 없으면 예외
        return bedAssignmentMapper.toDto(entity); // Entity -> DTO 변환해서 반환
    }

    // [생성] 새 배정 만들기 = "이 환자를 이 병상에 배정한다"
    // @Transactional: 이 메서드 안에서 일어나는 여러 DB 작업(배정 저장 + 병상 상태 저장)을 하나로 묶음
    //                 → 중간에 예외 나면 전부 롤백(둘 다 취소), 성공하면 전부 커밋
    @Transactional
    @Override
    public BedAssignmentDTO createBedAssignment(BedAssignmentDTO requestDto) {
        // 1) 먼저 이 병상이 "배정 가능한 상태"인지 검증 (이미 다른 사람이 쓰고 있으면 예외 던지고 여기서 끝)
        validateBedAvailable(requestDto.getBedId());
        // 2) 요청받은 DTO를 DB에 저장할 Entity로 변환
        BedAssignmentEntity entity = bedAssignmentMapper.toEntity(requestDto);
        // 3) 배정 Entity를 DB에 저장 (BED_ASSIGNMENT 테이블에 INSERT)
        BedAssignmentEntity saved = bedAssignmentRepository.save(entity);
        // 4) 배정이 생겼으니, 그 병상(BED 테이블)의 상태를 OCCUPIED(사용중)로 바꿈
        //    → "배정 테이블"과 "병상 테이블"은 다른 테이블이라 따로 업데이트해줘야 함
        markBedOccupied(saved.getBedId());
        // 5) 저장된 결과를 DTO로 변환해서 반환
        return bedAssignmentMapper.toDto(saved);
    }

    // [수정] 기존 배정을 고치기 — 대표적으로 "퇴상 처리"(releasedAt 채우기)가 여기로 옴
    @Transactional
    @Override
    public BedAssignmentDTO updateBedAssignment(Long assignmentId, BedAssignmentDTO requestDto) {
        // 1) 수정할 배정을 id로 먼저 찾음 (없으면 예외)
        BedAssignmentEntity entity = bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));

        // 2) 요청받은 값으로 필드들을 덮어씀 (아직 DB에 반영 안 됨, 메모리 상의 객체만 수정)
        entity.setBedId(requestDto.getBedId());
        entity.setAssignedAt(requestDto.getAssignedAt());
        entity.setReleasedAt(requestDto.getReleasedAt());

        // 3) 수정된 내용을 DB에 저장 (UPDATE)
        BedAssignmentEntity updated = bedAssignmentRepository.save(entity);
        // 4) 방금 저장한 releasedAt이 null이 아니면 = "이번에 퇴상 처리가 된 것"
        //    → 그 병상을 다시 EMPTY(빈 병상)로 되돌림
        if (updated.getReleasedAt() != null) {
            markBedEmpty(updated.getBedId());
        }
        // 5) 저장된 결과를 DTO로 변환해서 반환
        return bedAssignmentMapper.toDto(updated);
    }

    // [삭제] 배정 자체를 지우기 (퇴상 처리가 아니라 레코드를 완전히 삭제하는 경우)
    @Transactional
    @Override
    public void deleteBedAssignment(Long assignmentId) {
        // 1) 지울 배정을 id로 찾음
        BedAssignmentEntity entity = bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        // 2) 배정 row를 DB에서 삭제
        bedAssignmentRepository.delete(entity);
        // 3) 삭제하려던 배정이 "아직 퇴상 안 된(releasedAt == null) 활성 배정"이었다면
        //    → 그 배정이 없어졌으니 병상도 다시 EMPTY로 되돌려야 함
        //    (이미 퇴상된 배정이었다면 그 병상은 이미 예전에 EMPTY 처리가 됐을 것이므로 다시 건드릴 필요 없음)
        if (entity.getReleasedAt() == null) {
            markBedEmpty(entity.getBedId());
        }
    }

    // [검증 전용 private 메서드] "이 병상, 지금 새로 배정해도 되는 상태냐?"만 확인
    // → create에서만 씀. 다른 곳에서는 쓸 필요 없음 (조회/수정/삭제엔 이 검증이 필요 없으니까)
    private void validateBedAvailable(String bedId) {
        // 이 병상에 "아직 퇴상 안 된(releasedAt이 null인)" 배정이 이미 있는지 조회
        BedAssignmentEntity existingAssignment = bedAssignmentRepository.findByBedIdAndReleasedAtIsNull(bedId);
        if (existingAssignment != null) {
            // 이미 누가 쓰고 있는 병상이면 배정 생성을 막음 (예외 던지면 여기서 메서드 실행이 멈춤)
            throw new BusinessException(ErrorCode.BED_ALREADY_OCCUPIED);
        }
        return; // 문제 없으면 그냥 정상 종료 (아무 값도 반환할 필요 없는 void 메서드)
    }

    // [병상 상태 변경 전용 private 메서드] bedId로 병상(BED 테이블 row)을 찾아서 OCCUPIED로 바꿈
    // → create에서만 씀 ("새로 배정됐다" = "병상이 사용중이 됐다")
    private void markBedOccupied(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        entity.setBedStatus(BedStatus.OCCUPIED); // 메모리 상의 상태만 변경
        bedRepository.save(entity);              // DB에 실제로 반영(UPDATE)
    }

    // [병상 상태 변경 전용 private 메서드] bedId로 병상을 찾아서 EMPTY로 바꿈
    // → update(퇴상 시)와 delete(활성 배정 삭제 시), 두 군데에서 씀
    //   ("배정이 끝났다/없어졌다" = "병상이 다시 비었다"라는 같은 의미라서 재사용)
    private void markBedEmpty(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        entity.setBedStatus(BedStatus.EMPTY);
        bedRepository.save(entity);
    }

}
