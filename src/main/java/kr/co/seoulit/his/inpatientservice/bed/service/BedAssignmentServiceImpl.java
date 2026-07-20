package kr.co.seoulit.his.inpatientservice.bed.service;

import jakarta.persistence.EntityNotFoundException;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentRequest;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedAssignmentResponse;
import kr.co.seoulit.his.inpatientservice.bed.entity.Bed;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignment;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedAssignmentMapper;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BedAssignmentServiceImpl implements BedAssignmentService {

    private final BedAssignmentRepository bedAssignmentRepository;
    private final BedRepository bedRepository;
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
        Bed bed = findBed(request.getBedId());
        if (bed.getBedStatus() != BedStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "사용 가능한 병상이 아닙니다. bedId=" + bed.getBedId() + ", status=" + bed.getBedStatus());
        }
        if (bedAssignmentRepository.existsByAdmissionIdAndReleasedAtIsNull(request.getAdmissionId())) {
            throw new IllegalStateException("이미 병상이 배정된 입원 건입니다. admissionId=" + request.getAdmissionId());
        }

        BedAssignment bedAssignment = bedAssignmentRepository.save(bedAssignmentMapper.toEntity(request));
        bed.changeStatus(BedStatus.OCCUPIED);

        return bedAssignmentMapper.toResponse(bedAssignment);
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

    @Override
    @Transactional
    public BedAssignmentResponse releaseBedAssignment(Long assignmentId) {
        BedAssignment bedAssignment = findBedAssignment(assignmentId);
        if (bedAssignment.getReleasedAt() != null) {
            throw new IllegalStateException("이미 해제된 배정입니다. assignmentId=" + assignmentId);
        }
        Bed bed = findBed(bedAssignment.getBedId());

        bedAssignment.release(LocalDateTime.now());
        bed.changeStatus(BedStatus.AVAILABLE);

        return bedAssignmentMapper.toResponse(bedAssignment);
    }

    private BedAssignment findBedAssignment(Long assignmentId) {
        return bedAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 병상배정입니다. assignmentId=" + assignmentId));
    }

    private Bed findBed(String bedId) {
        return bedRepository.findById(bedId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 병상입니다. bedId=" + bedId));
    }
}
