package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.model.DealStatus;

@Getter
@Setter
public class AuctionDealRequest {
    private Long lotId;
    private Long winningBidId;
    private DealStatus transferStatus;
}
