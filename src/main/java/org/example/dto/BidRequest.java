package org.example.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BidRequest {
    private Long lotId;
    private Long bidderId;
    private BigDecimal amount;
}
