package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.bed.dto.BedDTO;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedEntity;
import kr.co.seoulit.his.inpatientservice.bed.mapper.BedMapper;
import kr.co.seoulit.his.inpatientservice.bed.entity.BedAssignmentEntity;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedAssignmentRepository;
import kr.co.seoulit.his.inpatientservice.bed.repository.BedRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BedServiceImpl implements BedService {
    private final BedRepository bedRepository;
    private final BedMapper bedMapper;
    private final BedAssignmentRepository bedAssignmentRepository;
    private final AdmissionRepository admissionRepository;

    public BedServiceImpl(BedRepository bedRepository,
            BedMapper bedMapper, BedAssignmentRepository bedAssignmentRepository,
            AdmissionRepository admissionRepository) {
        this.bedRepository = bedRepository;
        this.bedMapper = bedMapper;
        this.bedAssignmentRepository = bedAssignmentRepository;
        this.admissionRepository = admissionRepository;
    }

    @Override
    public List<BedDTO> getBeds() {
        return bedRepository.findAll().stream()
                .map(bedMapper::toDto)
                .map(dto -> enrichWithPatientId(dto, dto.getBedId()))
                .toList();
    }

    private BedDTO enrichWithPatientId(BedDTO dto, String bedId) {
        BedAssignmentEntity activeAssignment = bedAssignmentRepository.findByBedIdAndReleasedAtIsNull(bedId);
        if (activeAssignment != null) {
            admissionRepository.findById(activeAssignment.getAdmissionId())
                    .ifPresent(admission -> dto.setPatientId(admission.getPatientId()));
        }
        return dto;
    }

    @Override
    public BedDTO getBed(String bedId) {
        BedEntity entity = bedRepository.findById(bedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        BedDTO dto = bedMapper.toDto(entity);
        // [조회] 병상 하나만 id로 가져오기 — 이것도 그냥 읽기만, 상태 변경 없음
        BedAssignmentEntity activeAssignment = bedAssignmentRepository.findByBedIdAndReleasedAtIsNull(bedId);

        if (activeAssignment != null) {
            admissionRepository.findById(activeAssignment.getAdmissionId())
                    .ifPresent(admission -> dto.setPatientId(admission.getPatientId()));
        }

        return dto;
    }
}
