package kr.co.seoulit.his.inpatientservice.nursing.service;

import jakarta.transaction.Transactional;
import kr.co.seoulit.his.inpatientservice.common.aop.HistoryTrackable;
import kr.co.seoulit.his.inpatientservice.common.aop.TracksHistory;
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
    private final NursingRecordValidator nursingRecordValidator;
    private final VitalSignMapper vitalSignMapper;
    private final VitalSignHistoryRepository vitalSignHistoryRepository;

    @Override
    public void recordHistory(String id,String changeType){
        VitalSignEntity entity = vitalSignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vital sign not found with ID: "+id));
        vitalSignHistoryRepository.save(toHistorySnapshot(entity, changeType));
    }
    @Override
    public List<VitalSignDTO> getVitalSigns(String admissionId) {
        // admissionId가 있으면 그 입원 건의 기록만 조회 — 전체를 받아 화면에서 거르지 않도록 (단독 목록 페이지는 값 없이 호출 → 전체)
        List<VitalSignEntity> entities = (admissionId == null || admissionId.isBlank())
                ? vitalSignRepository.findAll()
                : vitalSignRepository.findByAdmissionId(admissionId);
        return entities.stream()
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
        nursingRecordValidator.validateWritable(requestDto.getAdmissionId());
        nursingRecordValidator.validateRecordTime(requestDto.getAdmissionId(), requestDto.getMeasuredAt());
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
    // 변경이력은 HistoryTrackingAspect가 실행 직전에 저장 (어노테이션은 구현 메서드에 있어야 AOP가 적용됨)
    // @Transactional: AOP가 남기는 이력과 실제 수정을 하나로 묶음 → 검증 실패 등으로 수정이 취소되면 이력도 남지 않음
    @Transactional
    @TracksHistory(changeType = "UPDATED")
    @Override
    public VitalSignDTO updateVitalSign(String vitalSignId,VitalSignDTO requestDto){
        return vitalSignRepository.findById(vitalSignId)
                .map(entity ->{
                    nursingRecordValidator.validateWritable(entity.getAdmissionId());
                    // 기록 시각을 보낸 경우만 검증 (비어 있으면 기존 값 유지)
                    if (requestDto.getMeasuredAt() != null) {
                        nursingRecordValidator.validateRecordTime(entity.getAdmissionId(), requestDto.getMeasuredAt());
                    }
                    vitalSignMapper.updateEntityFromDto(entity, requestDto);
                    return vitalSignMapper.toDto(vitalSignRepository.save(entity));

                })
                .orElseThrow(() -> new RuntimeException("Vital sign not found with ID: " + vitalSignId));
    }
    @Transactional
    @TracksHistory(changeType = "DELETED")
    @Override
    public void deleteVitalSign(String vitalSignId) {
        // Implementation for deleting a specific vital sign
        VitalSignEntity entity = vitalSignRepository.findById(vitalSignId)
                .orElseThrow(()->new RuntimeException("Vital sign not found with ID: " + vitalSignId));
        nursingRecordValidator.validateWritable(entity.getAdmissionId());
        vitalSignRepository.deleteById(vitalSignId);
    }
}
