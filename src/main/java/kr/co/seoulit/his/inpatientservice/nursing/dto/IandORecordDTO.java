package kr.co.seoulit.his.inpatientservice.nursing.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IandORecordDTO {
    private String intakeOutputId;
    private String admissionId;
    private LocalDateTime recordedAt;
    private String ioTypeCd;
    private String routeCd;
    private Integer amountMl;
    private String recorderId; // 기록자 = admin 직원 ID(empId, 간호사) — 숫자 직접 입력에서 직원 선택으로 바뀌며 문자열로 변경
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
