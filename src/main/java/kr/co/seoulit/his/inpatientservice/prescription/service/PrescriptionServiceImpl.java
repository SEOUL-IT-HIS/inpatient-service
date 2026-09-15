package kr.co.seoulit.his.inpatientservice.prescription.service;

import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.prescription.entity.PrescriptionEntity;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionItemDTO;
import kr.co.seoulit.his.inpatientservice.prescription.client.PrescriptionCoreClient;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionCreateDTO;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionDTO;
import kr.co.seoulit.his.inpatientservice.prescription.mapper.PrescriptionMapper;
import kr.co.seoulit.his.inpatientservice.prescription.repository.PrescriptionItemRepository;
import kr.co.seoulit.his.inpatientservice.prescription.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService{
    private final PrescriptionCoreClient prescriptionCoreClient;
    private final PrescriptionRepository prescriptionRepository;
    private final AdmissionRepository admissionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PrescriptionMapper prescriptionMapper;

    public PrescriptionServiceImpl(PrescriptionCoreClient prescriptionCoreClient, PrescriptionRepository prescriptionRepository, AdmissionRepository admissionRepository, PrescriptionItemRepository prescriptionItemRepository, PrescriptionMapper prescriptionMapper) {
        this.prescriptionCoreClient = prescriptionCoreClient;
        this.prescriptionRepository = prescriptionRepository;
        this.admissionRepository = admissionRepository;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.prescriptionMapper = prescriptionMapper;
    }

    @Override
    public PrescriptionDTO createPrescription(String admissionId, PrescriptionCreateDTO requestDto) {
        AdmissionEntity admission = admissionRepository.findById(admissionId)
                .orElseThrow(()->new BusinessException(ErrorCode.ADMISSION_NOT_FOUND));
        requestDto.setPatientId(admission.getPatientId());

        PrescriptionDTO created = prescriptionCoreClient.createPrescription(admissionId, requestDto);
        created.setAdmissionId(admissionId);

        prescriptionRepository.save(prescriptionMapper.toEntity(created));

        if(created.getItems() != null){
            for(PrescriptionItemDTO itemDTO : created.getItems()) {
                itemDTO.setPrescriptionId(created.getPrescriptionId());
                prescriptionItemRepository.save(prescriptionMapper.toItemEntity(itemDTO));
            }
        }

        return created;
    }

    @Override
    public List<PrescriptionDTO> getPrescriptionsByAdmission(String admissionId) {
        return prescriptionRepository.findByAdmissionId(admissionId).stream()
                .map(this::toDtoWithItems)
                .toList();
    }

    @Override
    public PrescriptionDTO getPrescription(String prescriptionId) {
        PrescriptionEntity entity = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(()-> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        return toDtoWithItems(entity);
    }
    private PrescriptionDTO toDtoWithItems(PrescriptionEntity entity){
        PrescriptionDTO dto = prescriptionMapper.toDto(entity);
        dto.setItems(prescriptionItemRepository.findByPrescriptionId(entity.getPrescriptionId()).stream()
                .map(prescriptionMapper::toItemDto)
                .toList());
        return dto;
    }

}
