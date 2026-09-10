package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.common.aop.HistoryTrackable;
import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.VitalSignEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.VitalSignHistoryEntity;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.VitalSignMapper;
import kr.co.seoulit.his.inpatientservice.nursing.repository.VitalSignHistoryRepository;
import kr.co.seoulit.his.inpatientservice.nursing.repository.VitalSignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VitalSignServiceImpl implements VitalSignService, HistoryTrackable {
    private final VitalSignRepository vitalSignRepository;
    private final VitalSignMapper vitalSignMapper;
    private final VitalSignHistoryRepository vitalSignHistoryRepository;

    @Override
    public void recordHistory(String id,String changeType){
        VitalSignEntity entity = vitalSignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vital sign not found with ID: "+id));
        vitalSignHistoryRepository.save(toHistorySnapshot(entity, changeType));
    }
    @Override
    public List<VitalSignDTO> getVitalSigns() {
        return vitalSignRepository.findAll().stream()
                .map(vitalSignMapper::toDto)
                .toList();
    }
    @Override
    public List<VitalSignHistoryDTO> getVitalSignHistory(String vitalSignId) {
        return vitalSignHistoryRepository.findByVitalSignIdOrderByChangedAtDesc(vitalSignId).stream()
                .map(vitalSignMapper::toDto)
                .toList();
    }

    @Override
    public VitalSignDTO createVitalSign(VitalSignDTO requestDto) {
        VitalSignEntity entity = vitalSignMapper.toEntity(requestDto);
        entity.setVitalSignId(UUID.randomUUID().toString());
        VitalSignDTO savedDto = vitalSignMapper.toDto(vitalSignRepository.save(entity));
        return savedDto;

    }

    @Override
    public VitalSignDTO getVitalSign(String vitalSignId) {
        // Implementation for retrieving a specific vital sign
        VitalSignDTO vitalSignDTO = vitalSignRepository.findById(vitalSignId)
                .map(vitalSignMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Vital sign not found with ID: " + vitalSignId));
        return vitalSignDTO;
    }

    private VitalSignHistoryEntity toHistorySnapshot(VitalSignEntity entity, String changeType){
        return VitalSignHistoryEntity.builder()
                .vitalSignId(entity.getVitalSignId())
                .admissionId(entity.getAdmissionId())
                .measuredAt(entity.getMeasuredAt())
                .temperature(entity.getTemperature())
                .pulse(entity.getPulse())
                .respiration(entity.getRespiration())
                .bpSystolic(entity.getBpSystolic())
                .bpDiastolic(entity.getBpDiastolic())
                .spo2(entity.getSpo2())
                .recorderId(entity.getRecorderId())
                .changeType(changeType)
                .build();
    }
    @Override
    public VitalSignDTO updateVitalSign(String vitalSignId,VitalSignDTO requestDto){
        return vitalSignRepository.findById(vitalSignId)
                .map(entity ->{
                    vitalSignHistoryRepository.save(toHistorySnapshot(entity,"UPDATED"));
                    vitalSignMapper.updateEntityFromDto(entity, requestDto);
                    return vitalSignMapper.toDto(vitalSignRepository.save(entity));

                })
                .orElseThrow(() -> new RuntimeException("Vital sign not found with ID: " + vitalSignId));
    }
    @Override
    public void deleteVitalSign(String vitalSignId) {
        // Implementation for deleting a specific vital sign
        VitalSignEntity entity = vitalSignRepository.findById(vitalSignId)
                .orElseThrow(()->new RuntimeException("Vital sign not found with ID: " + vitalSignId));
        vitalSignHistoryRepository.save(toHistorySnapshot(entity,"DELETED"));
        vitalSignRepository.deleteById(vitalSignId);
    }
}
