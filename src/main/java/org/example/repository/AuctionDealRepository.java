package org.example.repository;

import org.example.model.AuctionDeal;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuctionDealRepository extends InMemoryRepository<AuctionDeal> {

    public List<AuctionDeal> findByLotId(Long lotId) {
        return findBy(deal -> lotId != null && lotId.equals(deal.getLotId()));
    }

    public List<AuctionDeal> findByWinningBidId(Long winningBidId) {
        return findBy(deal -> winningBidId != null && winningBidId.equals(deal.getWinningBidId()));
    }
}

