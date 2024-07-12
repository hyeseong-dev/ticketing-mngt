package com.mgnt.concertservice.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventory")
public class Inventory extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @NotNull
    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @NotNull
    @Column(name = "concert_date_id", nullable = false)
    private Long concertDateId;

    @NotNull
    @Column(name = "total", nullable = false)
    @ColumnDefault("0")
    private Long total = 0L;

    @NotNull
    @Column(name = "remaining", nullable = false)
    @ColumnDefault("0")
    private Long remaining = 0L;

    @ColumnDefault("0")
    @Column(name = "version")
    private Long version = 0L;

    @Builder
    public Inventory(Long concertId, Long concertDateId, Long total, Long remaining, Long version) {
        this.concertId = concertId;
        this.concertDateId = concertDateId;
        this.total = total;
        this.remaining = remaining;
        this.version = version;
    }
}
