package kr.co.seoulit.his.inpatientservice.bed.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "BED")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Bed {

    @Id
    @Column(name = "BED_ID", length = 18)
    private String bedId;

    @Column(name = "ROOM_NO", length = 18, nullable = false)
    private String roomNo;

    @Column(name = "BED_NO", length = 18, nullable = false)
    private String bedNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "BED_STATUS", length = 18, nullable = false)
    private BedStatus bedStatus;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", insertable = false)
    private LocalDateTime updatedAt;

    public void changeStatus(BedStatus bedStatus) {
        this.bedStatus = bedStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
