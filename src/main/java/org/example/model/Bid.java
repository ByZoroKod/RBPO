package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "bids")
public class Bid extends BaseEntity {

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(name = "bidder_id", nullable = false)
    private Long bidderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "placed_at", nullable = false)
    private Instant time;
}
