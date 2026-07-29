package kr.co.seoulit.his.inpatientservice.admission.service;

import kr.co.seoulit.his.inpatientservice.admission.dto.AdmissionDTO;
import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.admission.mapper.AdmissionMapper;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdmissionServiceImpl implements AdmissionService {
    private final AdmissionRepository admissionRepository;
    private final AdmissionMapper admissionMapper;


    public AdmissionServiceImpl(AdmissionRepository admissionRepository, AdmissionMapper admissionMapper) {
        this.admissionRepository = admissionRepository;
        this.admissionMapper = admissionMapper;
    }
    @Override
    public AdmissionDTO receiveAdmission(AdmissionDTO requestDto){
        AdmissionEntity entity = admissionMapper.toEntity(requestDto);
        return admissionMapper.toDto(admissionRepository.save(entity));
    }

    @Override
    public List<AdmissionDTO> getAdmissions() {
        return admissionRepository.findAll().stream()
                .map(admissionMapper::toDto)
                .toList();
    }

    @Override
    public AdmissionDTO createAdmission(AdmissionDTO requestDto) {
        AdmissionEntity entity = admissionMapper.toEntity(requestDto);
        return admissionMapper.toDto(admissionRepository.save(entity));
    }

    @Override
    public AdmissionDTO getAdmission(String admissionId) {
        AdmissionEntity entity = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));
        return admissionMapper.toDto(entity);
    }

    @Override
    public AdmissionDTO updateAdmission(String admissionId, AdmissionDTO requestDto) {
        AdmissionEntity entity = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));

        entity.setPatientId(requestDto.getPatientId());
        entity.setAdmissionDate(requestDto.getAdmissionDate());
        entity.setStatus(requestDto.getStatus());
        return admissionMapper.toDto(admissionRepository.save(entity));
    }
    @Override
    public AdmissionDTO changeStatus(String admissionId, String status){
        AdmissionEntity entity = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BED_ASSIGNMENT_NOT_FOUND));

        entity.setStatus(status);
        return admissionMapper.toDto(admissionRepository.save(entity));
    }


}
