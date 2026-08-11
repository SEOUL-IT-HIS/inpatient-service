package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignDTO;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.VitalSignMapper;
import kr.co.seoulit.his.inpatientservice.nursing.repository.VitalSignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VitalSignServiceImpl implements VitalSignService {
    private final VitalSignRepository vitalSignRepository;
    private final VitalSignMapper vitalSignMapper;

    @Override
    public List<VitalSignDTO> getVitalSigns() {
        return vitalSignRepository.findAll().stream()
                .map(vitalSignMapper::toDto)
                .toList();
    }

    @Override
    public VitalSignDTO createVitalSign(VitalSignDTO requestDto) {
        VitalSignDTO savedDto = vitalSignMapper.toDto(vitalSignRepository.save(vitalSignMapper.toEntity(requestDto)));
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

    @Override
    public VitalSignDTO updateVitalSign(String vitalSignId, VitalSignDTO requestDto) {
        // Implementation for updating a specific vital sign
        VitalSignDTO vitalSignDTO = vitalSignRepository.findById(vitalSignId)
                .map(entity -> {
                    // Update the entity with values from requestDto
                    vitalSignMapper.updateEntityFromDto(entity, requestDto);
                    return vitalSignMapper.toDto(vitalSignRepository.save(entity));
                })
                .orElseThrow(() -> new RuntimeException("Vital sign not found with ID: " + vitalSignId));
        return vitalSignDTO;
    }

    @Override
    public void deleteVitalSign(String vitalSignId) {
        // Implementation for deleting a specific vital sign
        vitalSignRepository.deleteById(vitalSignId);
    }

}
