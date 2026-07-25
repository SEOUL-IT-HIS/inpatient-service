package kr.co.seoulit.his.inpatientservice.bed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bed_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bed_assignment_seq")
    @SequenceGenerator(name = "bed_assignment_seq", sequenceName = "bed_assignment_seq", allocationSize = 1)
    private Long assignmentId;

    private String bedId;
    private String admissionId;
    private LocalDateTime assignedAt;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
