package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AuctionDeal extends BaseEntity {
    private Long lotId;
    private Long winningBidId;
    private BigDecimal amount;
    private DealStatus transferStatus;
}
