package kr.co.seoulit.his.inpatientservice.nursing.service;

import kr.co.seoulit.his.inpatientservice.admission.entity.AdmissionEntity;
import kr.co.seoulit.his.inpatientservice.admission.repository.AdmissionRepository;
import kr.co.seoulit.his.inpatientservice.common.aop.HistoryTrackingAspect;
import kr.co.seoulit.his.inpatientservice.common.config.TransactionConfig;
import kr.co.seoulit.his.inpatientservice.common.exception.BusinessException;
import kr.co.seoulit.his.inpatientservice.common.exception.ErrorCode;
import kr.co.seoulit.his.inpatientservice.common.util.DateRules;
import kr.co.seoulit.his.inpatientservice.nursing.dto.VitalSignDTO;
import kr.co.seoulit.his.inpatientservice.nursing.entity.VitalSignEntity;
import kr.co.seoulit.his.inpatientservice.nursing.entity.VitalSignHistoryEntity;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.IandORecordMapperImpl;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.NursingAssessmentMapperImpl;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.RestraintMapperImpl;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.RiskAssessmentMapperImpl;
import kr.co.seoulit.his.inpatientservice.nursing.mapper.VitalSignMapperImpl;
import kr.co.seoulit.his.inpatientservice.nursing.repository.VitalSignHistoryRepository;
import kr.co.seoulit.his.inpatientservice.nursing.repository.VitalSignRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 간호기록 변경이력 AOP 검증 — @TracksHistory가 실제로 적용되는지, 수정/삭제 1건에 이력이 정확히 1건 남는지
// (예전에는 어노테이션이 인터페이스에만 있어 AOP가 적용되지 않았고, 이력은 서비스 코드가 직접 저장하고 있었음)
@DataJpaTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({HistoryTrackingAspect.class, NursingHistoryAopTest.AopConfig.class, TransactionConfig.class, NursingRecordTimeValidator.class,
        VitalSignServiceImpl.class, IandORecordServiceImpl.class, NursingAssessmentServiceImpl.class,
        RestraintServiceImpl.class, RiskAssessmentServiceImpl.class,
        VitalSignMapperImpl.class, IandORecordMapperImpl.class, NursingAssessmentMapperImpl.class,
        RestraintMapperImpl.class, RiskAssessmentMapperImpl.class})
class NursingHistoryAopTest {

    // 슬라이스 테스트에는 AOP 자동 설정이 없어서 앱과 같은 방식(CGLIB 프록시)으로 켜 줌
    @TestConfiguration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class AopConfig {
    }

    @Autowired VitalSignService vitalSignService;
    @Autowired IandORecordService iandORecordService;
    @Autowired NursingAssessmentService nursingAssessmentService;
    @Autowired RestraintService restraintService;
    @Autowired RiskAssessmentService riskAssessmentService;
    @Autowired VitalSignRepository vitalSignRepository;
    @Autowired VitalSignHistoryRepository vitalSignHistoryRepository;
    @Autowired AdmissionRepository admissionRepository;

    @AfterEach
    void cleanUp() {
        vitalSignHistoryRepository.deleteAll();
        vitalSignRepository.deleteAll();
        admissionRepository.deleteAll();
    }

    @Test
    void 간호기록_5종_서비스_모두에_변경이력_AOP가_적용된다() {
        assertThat(List.of(vitalSignService, iandORecordService, nursingAssessmentService,
                restraintService, riskAssessmentService))
                .allMatch(AopUtils::isAopProxy);
    }

    @Test
    void 수정하면_수정_직전_값으로_이력이_정확히_1건_남는다() {
        saveVitalSign("VS1", 36);
        VitalSignDTO dto = vitalSignService.getVitalSign("VS1");
        dto.setTemperature(38);

        vitalSignService.updateVitalSign("VS1", dto);

        List<VitalSignHistoryEntity> histories = vitalSignHistoryRepository.findByVitalSignIdOrderByChangedAtDesc("VS1");
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getChangeType()).isEqualTo("UPDATED");
        assertThat(histories.get(0).getTemperature()).isEqualTo(36);
        assertThat(vitalSignRepository.findById("VS1").orElseThrow().getTemperature()).isEqualTo(38);
    }

    @Test
    void 삭제하면_삭제_직전_값으로_DELETED_이력이_남는다() {
        saveVitalSign("VS2", 37);

        vitalSignService.deleteVitalSign("VS2");

        List<VitalSignHistoryEntity> histories = vitalSignHistoryRepository.findByVitalSignIdOrderByChangedAtDesc("VS2");
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getChangeType()).isEqualTo("DELETED");
        assertThat(histories.get(0).getTemperature()).isEqualTo(37);
        assertThat(vitalSignRepository.findById("VS2")).isEmpty();
    }

    @Test
    void 미래_시각으로_수정하면_거절되고_AOP가_남긴_이력도_롤백된다() {
        saveVitalSign("VS3", 36);
        VitalSignDTO dto = vitalSignService.getVitalSign("VS3");
        dto.setMeasuredAt(DateRules.now().plusHours(2));

        assertThatThrownBy(() -> vitalSignService.updateVitalSign("VS3", dto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NURSING_RECORD_TIME_INVALID);

        assertThat(vitalSignHistoryRepository.findByVitalSignIdOrderByChangedAtDesc("VS3")).isEmpty();
        assertThat(vitalSignRepository.findById("VS3").orElseThrow().getTemperature()).isEqualTo(36);
    }

    @Test
    void 입원일_이전_시각이나_미래_시각으로는_기록을_등록할_수_없다() {
        admissionRepository.save(AdmissionEntity.builder()
                .admissionId("ADM9").patientId("P9").status("ADMITTED")
                .admissionDate(DateRules.today().minusDays(1).atTime(15, 0)).build());

        VitalSignDTO beforeAdmission = new VitalSignDTO();
        beforeAdmission.setAdmissionId("ADM9");
        beforeAdmission.setMeasuredAt(DateRules.today().minusDays(2).atTime(10, 0));
        assertThatThrownBy(() -> vitalSignService.createVitalSign(beforeAdmission))
                .extracting("errorCode").isEqualTo(ErrorCode.NURSING_RECORD_TIME_INVALID);

        VitalSignDTO future = new VitalSignDTO();
        future.setAdmissionId("ADM9");
        future.setMeasuredAt(DateRules.now().plusDays(1));
        assertThatThrownBy(() -> vitalSignService.createVitalSign(future))
                .extracting("errorCode").isEqualTo(ErrorCode.NURSING_RECORD_TIME_INVALID);

        // 입원 당일(입원 시각보다 이른 시각이어도 같은 날) 기록은 허용
        VitalSignDTO admissionDay = new VitalSignDTO();
        admissionDay.setAdmissionId("ADM9");
        admissionDay.setMeasuredAt(DateRules.today().minusDays(1).atTime(9, 0));
        assertThat(vitalSignService.createVitalSign(admissionDay).getVitalSignId()).isNotNull();
    }

    private void saveVitalSign(String id, int temperature) {
        vitalSignRepository.save(VitalSignEntity.builder()
                .vitalSignId(id)
                .admissionId("ADM1")
                .measuredAt(DateRules.now().minusHours(1))
                .temperature(temperature)
                .build());
    }
}
