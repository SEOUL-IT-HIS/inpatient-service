package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedListResponse;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedResponse;
import kr.co.seoulit.his.inpatientservice.bed.entity.Bed;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignment;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedStatus;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedStatusCount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BedServiceImpl implements BedService {

    private final BedRepository bedRepository;
    private final BedAssignmentRepository bedAssignmentRepository;

    @Override
    public BedListResponse getBeds(BedStatus status, Pageable pageable) {
        Page<Bed> page = (status != null)
                ? bedRepository.findByBedStatus(status, pageable)
                : bedRepository.findAll(pageable);

        return BedListResponse.builder()
                .beds(page.getContent().stream().map(this::toResponse).toList())
                .statusCounts(getStatusCounts())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    private Map<BedStatus, Long> getStatusCounts() {
        Map<BedStatus, Long> statusCounts = new EnumMap<>(BedStatus.class);
        for (BedStatus bedStatus : BedStatus.values()) {
            statusCounts.put(bedStatus, 0L);
        }
        for (BedStatusCount count : bedRepository.countByStatus()) {
            statusCounts.put(count.getStatus(), count.getCount());
        }
        return statusCounts;
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
