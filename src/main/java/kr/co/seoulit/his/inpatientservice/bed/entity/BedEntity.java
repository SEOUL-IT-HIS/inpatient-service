package kr.co.seoulit.his.inpatientservice.bed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bed")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedEntity {

    @Id
    private String bedId;

    //@Column(nullable = false)
    private String wardCd;

    @Column(nullable = false)
    private String roomNo;

    @Column(nullable = false)
    private String bedNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedStatus bedStatus;

    private String patientId;

    // 공통코드(admin-service) 병실유형 항목의 codeValue를 그대로 저장 (예: 1인실/다인실)
    private String roomTypeCode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
