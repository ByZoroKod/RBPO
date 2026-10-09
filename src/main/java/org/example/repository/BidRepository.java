package org.example.repository;

import org.example.model.Bid;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BidRepository extends InMemoryRepository<Bid> {

    public List<Bid> findByLotId(Long lotId) {
        return findBy(bid -> lotId != null && lotId.equals(bid.getLotId()));
    }

    public List<Bid> findByBidderId(Long bidderId) {
        return findBy(bid -> bidderId != null && bidderId.equals(bid.getBidderId()));
    }
}

