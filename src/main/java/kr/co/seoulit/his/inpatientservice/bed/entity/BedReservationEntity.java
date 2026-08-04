package kr.co.seoulit.his.inpatientservice.bed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bed_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bed_reservation_seq")
    @SequenceGenerator(name = "bed_reservation_seq", sequenceName = "bed_reservation_seq", allocationSize = 1)
    private Long bedReservationId;

    private String bedId;
    private String patientId;
    private LocalDateTime reserveAt;
    private LocalDateTime expectedAdmissionAt;
    private String reservationStatusCd;
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
