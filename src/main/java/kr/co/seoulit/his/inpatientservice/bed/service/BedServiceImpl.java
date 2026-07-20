package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedResponse;
import kr.co.seoulit.his.inpatientservice.bed.entity.Bed;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignment;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BedServiceImpl implements BedService {

    private final BedRepository bedRepository;
    private final BedAssignmentRepository bedAssignmentRepository;

    @Override
    public List<BedResponse> getBeds() {
        return bedRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private BedResponse toResponse(Bed bed) {
        BedAssignment activeAssignment = bedAssignmentRepository
                .findByBedIdAndReleasedAtIsNull(bed.getBedId())
                .orElse(null);

        return BedResponse.builder()
                .bedId(bed.getBedId())
                .roomNo(bed.getRoomNo())
                .bedNo(bed.getBedNo())
                .bedStatus(bed.getBedStatus())
                .admissionId(activeAssignment != null ? activeAssignment.getAdmissionId() : null)
                .assignedAt(activeAssignment != null ? activeAssignment.getAssignedAt() : null)
                .build();
    }
}
