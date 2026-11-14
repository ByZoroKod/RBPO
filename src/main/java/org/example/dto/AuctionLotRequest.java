package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.model.LotStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class AuctionLotRequest {
    private Long itemId;
    private BigDecimal startPrice;
    private BigDecimal step;
    private Instant startTime;
    private Instant endTime;
    private LotStatus status;
}
