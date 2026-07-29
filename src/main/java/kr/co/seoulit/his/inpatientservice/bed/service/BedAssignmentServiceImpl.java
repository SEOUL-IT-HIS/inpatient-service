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
    private final BedAssignmentRepository bedAssignmentRepository;
    private final BedAssignmentMapper bedAssignmentMapper;
    private final BedRepository bedRepository;

    public BedAssignmentServiceImpl(BedAssignmentRepository bedAssignmentRepository,
            BedAssignmentMapper bedAssignmentMapper,
            BedRepository bedRepository) {
        this.bedAssignmentRepository = bedAssignmentRepository;
        this.bedAssignmentMapper = bedAssignmentMapper;
        this.bedRepository = bedRepository;
    }

    @Override
    public List<BedAssignmentDTO> getBedAssignments() {
        return bedAssignmentRepository.findAll().stream()
                .map(bedAssignmentMapper::toDto)
                .toList();
    }

    @Override
    public BedAssignmentDTO getBedAssignment(Long assignmentId) {
        BedAssignmentEntity entity = bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        return bedAssignmentMapper.toDto(entity);
    }

    @Transactional
    @Override
    public BedAssignmentDTO createBedAssignment(BedAssignmentDTO requestDto) {
        validateBedAvailable(requestDto.getBedId());
        BedAssignmentEntity entity = bedAssignmentMapper.toEntity(requestDto);
        BedAssignmentEntity saved = bedAssignmentRepository.save(entity);
        markBedOccupied(saved.getBedId());
        return bedAssignmentMapper.toDto(saved);
    }

    @Transactional
    @Override
    public BedAssignmentDTO updateBedAssignment(Long assignmentId, BedAssignmentDTO requestDto) {
        BedAssignmentEntity entity = bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));

        entity.setBedId(requestDto.getBedId());
        entity.setAssignedAt(requestDto.getAssignedAt());
        entity.setReleasedAt(requestDto.getReleasedAt());

        BedAssignmentEntity updated = bedAssignmentRepository.save(entity);
        if (updated.getReleasedAt() != null) {
            markBedEmpty(updated.getBedId());
        }
        return bedAssignmentMapper.toDto(updated);
    }

    @Transactional
    @Override
    public void deleteBedAssignment(Long assignmentId) {
        BedAssignmentEntity entity = bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        bedAssignmentRepository.delete(entity);
        if (entity.getReleasedAt() == null) {
            markBedEmpty(entity.getBedId());
        }
    }

    private void validateBedAvailable(String bedId) {
        BedAssignmentEntity existingAssignment = bedAssignmentRepository.findByBedIdAndReleasedAtIsNull(bedId);
        if (existingAssignment != null) {
            throw new BusinessException(ErrorCode.BED_ALREADY_OCCUPIED);
        }
        return;
    }

    private void markBedOccupied(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        entity.setBedStatus(BedStatus.OCCUPIED);
        bedRepository.save(entity);
    }

    private void markBedEmpty(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_NOT_FOUND));
        entity.setBedStatus(BedStatus.EMPTY);
        bedRepository.save(entity);
    }

}
