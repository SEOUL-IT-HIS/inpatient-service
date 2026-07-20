package kr.co.seoulit.his.inpatientservice.bed.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "BED_ASSIGNMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BedAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ASSIGNMENT_ID")
    private Long assignmentId;

    @Column(name = "BED_ID", length = 18, nullable = false)
    private String bedId;

    @Column(name = "ADMISSION_ID", nullable = false)
    private Long admissionId;

    @Column(name = "ASSIGNED_AT", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "RELEASED_AT")
    private LocalDateTime releasedAt;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", insertable = false)
    private LocalDateTime updatedAt;

    public void update(String bedId, Long admissionId, LocalDateTime assignedAt, LocalDateTime releasedAt) {
        this.bedId = bedId;
        this.admissionId = admissionId;
        this.assignedAt = assignedAt;
        this.releasedAt = releasedAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void release(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
        this.updatedAt = LocalDateTime.now();
    }
}
