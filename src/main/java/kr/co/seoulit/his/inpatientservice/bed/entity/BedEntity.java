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

    @Column(nullable = false)
    private String roomNo;

    @Column(nullable = false)
    private String bedNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedStatus bedStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
