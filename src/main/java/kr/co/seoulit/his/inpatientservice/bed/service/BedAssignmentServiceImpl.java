package kr.co.seoulit.his.inpatientservice.bed.service;

import jakarta.transaction.Transactional;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignmentEntity;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;

import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class BedAssignmentServiceImpl implements BedAssignmentService {
    private final BedAssignmentRepository bedAssignmentRepository;
    private final BedAssignmentMapper bedAssignmentMapper;

    public BedAssignmentServiceImpl(BedAssignmentRepository bedAssignmentRepository,
            BedAssignmentMapper bedAssignmentMapper) {
        this.bedAssignmentRepository = bedAssignmentRepository;
        this.bedAssignmentMapper = bedAssignmentMapper;
    }

    @Override
    public List<BedAssignmentDTO> getBedAssignments() {
        return bedAssignmentRepository.findAll().stream()
                .map(bedAssignmentMapper::toDto)
                .toList();
    }

    @Override
    public BedAssignmentDTO getBedAssignment(String assignmentId) {
        BedAssignmentEntity entity = bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        return bedAssignmentMapper.toDto(entity);
    }

    @Override
    public BedAssignmentDTO createBedAssignment(BedAssignmentDTO requestDto) {
        BedAssignmentEntity entity = bedAssignmentMapper.toEntity(requestDto);
        return bedAssignmentMapper.toDto(bedAssignmentRepository.save(entity));
    }

    @Override
    public BedAssignmentDTO updateBedAssignment(String assignmentId, BedAssignmentDTO requestDto) {
        BedAssignmentEntity entity = bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));

        entity.setAssignmentId(requestDto.getAssignmentId());
        entity.setBedId(requestDto.getBedId());
        entity.setAssignedAt(requestDto.getAssignedAt());
        entity.setReleasedAt(requestDto.getReleasedAt());

        return bedAssignmentMapper.toDto(bedAssignmentRepository.save(entity));

    }

    @Override
    public void deleteBedAssignment(String assignmentId) {
        BedAssignmentEntity entity = bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        bedAssignmentRepository.delete(entity);
    }

}
