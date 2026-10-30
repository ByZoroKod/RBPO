package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "auction_deals")
public class AuctionDeal extends BaseEntity {

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(name = "winning_bid_id", nullable = false)
    private Long winningBidId;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "transfer_status", nullable = false, length = 20)
    private DealStatus transferStatus;
}
