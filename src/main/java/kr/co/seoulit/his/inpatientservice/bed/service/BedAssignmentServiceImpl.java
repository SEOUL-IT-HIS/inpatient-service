package kr.co.seoulit.his.inpatientservice.bed.service;

import jakarta.persistence.EntityNotFoundException;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentRequest;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentResponse;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignment;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BedAssignmentServiceImpl implements BedAssignmentService {

    private final BedAssignmentRepository bedAssignmentRepository;
    private final BedAssignmentMapper bedAssignmentMapper;

    @Override
    public List<BedAssignmentResponse> getBedAssignments() {
        return bedAssignmentRepository.findAll().stream()
                .map(bedAssignmentMapper::toResponse)
                .toList();
    }

    @Override
    public BedAssignmentResponse getBedAssignment(Long assignmentId) {
        return bedAssignmentMapper.toResponse(findBedAssignment(assignmentId));
    }

    @Override
    @Transactional
    public BedAssignmentResponse createBedAssignment(BedAssignmentRequest request) {
        BedAssignment bedAssignment = bedAssignmentMapper.toEntity(request);
        return bedAssignmentMapper.toResponse(bedAssignmentRepository.save(bedAssignment));
    }

    @Override
    @Transactional
    public BedAssignmentResponse updateBedAssignment(Long assignmentId, BedAssignmentRequest request) {
        BedAssignment bedAssignment = findBedAssignment(assignmentId);
        bedAssignment.update(
                request.getBedId(),
                request.getAdmissionId(),
                request.getAssignedAt(),
                request.getReleasedAt()
        );
        return bedAssignmentMapper.toResponse(bedAssignment);
    }

    private BedAssignment findBedAssignment(Long assignmentId) {
        return bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 병상배정입니다. assignmentId=" + assignmentId));
    }
}
