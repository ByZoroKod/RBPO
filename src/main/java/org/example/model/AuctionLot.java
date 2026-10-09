package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class AuctionLot extends BaseEntity {
    private Long itemId;
    private BigDecimal startPrice;
    private BigDecimal step;
    private Instant startTime;
    private Instant endTime;
    private LotStatus status;
}
