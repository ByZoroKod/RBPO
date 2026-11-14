package org.example.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class ListForAuctionRequest {
    private BigDecimal startPrice;
    private BigDecimal step;
    private Instant endTime;
}
