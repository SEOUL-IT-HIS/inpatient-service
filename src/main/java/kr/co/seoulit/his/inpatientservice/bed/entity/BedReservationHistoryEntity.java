package kr.co.seoulit.his.inpatientservice.bed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bed_reservation_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedReservationHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bed_reservation_history_seq")
    @SequenceGenerator(name = "bed_reservation_history_seq", sequenceName = "bed_reservation_history_seq", allocationSize = 1)
    private Long bedReservationHistoryId;

    private Long bedReservationId;

    @Enumerated(EnumType.STRING)
    private BedReservationStatus previousStatusCd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedReservationStatus newStatusCd;

    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
