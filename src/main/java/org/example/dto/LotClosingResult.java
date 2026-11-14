package org.example.dto;

import lombok.Getter;
import org.example.model.AuctionDeal;
import org.example.model.AuctionLot;

@Getter
public class LotClosingResult {
    private final AuctionLot lot;
    private final AuctionDeal deal;

    public LotClosingResult(AuctionLot lot, AuctionDeal deal) {
        this.lot = lot;
        this.deal = deal;
    }
}
