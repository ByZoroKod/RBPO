package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class Bid extends BaseEntity {
    private Long lotId;
    private Long bidderId;
    private BigDecimal amount;
    private Instant time;
}
