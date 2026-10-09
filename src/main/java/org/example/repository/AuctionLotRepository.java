package org.example.repository;

import org.example.model.AuctionLot;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuctionLotRepository extends InMemoryRepository<AuctionLot> {

    public List<AuctionLot> findByItemId(Long itemId) {
        return findBy(lot -> itemId != null && itemId.equals(lot.getItemId()));
    }
}

