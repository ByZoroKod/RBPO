package org.example.security;

import org.example.repository.AuctionDealRepository;
import org.example.repository.AuctionLotRepository;
import org.example.repository.BidRepository;
import org.example.repository.CollectibleItemRepository;
import org.springframework.stereotype.Component;

@Component("access")
public class AccessPolicy {

    private final CollectibleItemRepository itemRepository;
    private final AuctionLotRepository lotRepository;
    private final AuctionDealRepository dealRepository;
    private final BidRepository bidRepository;

    public AccessPolicy(CollectibleItemRepository itemRepository,
                        AuctionLotRepository lotRepository,
                        AuctionDealRepository dealRepository,
                        BidRepository bidRepository) {
        this.itemRepository = itemRepository;
        this.lotRepository = lotRepository;
        this.dealRepository = dealRepository;
        this.bidRepository = bidRepository;
    }

    public boolean ownsItem(Long userId, Long itemId) {
        return itemRepository.findById(itemId)
                .map(item -> item.getOwnerId().equals(userId))
                .orElse(false);
    }

    public boolean ownsLot(Long userId, Long lotId) {
        return lotRepository.findById(lotId)
                .map(lot -> ownsItem(userId, lot.getItemId()))
                .orElse(false);
    }

    public boolean isDealSeller(Long userId, Long dealId) {
        return dealRepository.findById(dealId)
                .map(deal -> userId.equals(deal.getSellerId()))
                .orElse(false);
    }

    public boolean participatesInDeal(Long userId, Long dealId) {
        return dealRepository.findById(dealId)
                .map(deal -> userId.equals(deal.getSellerId()) || isWinner(userId, deal.getWinningBidId()))
                .orElse(false);
    }

    private boolean isWinner(Long userId, Long bidId) {
        return bidRepository.findById(bidId)
                .map(bid -> bid.getBidderId().equals(userId))
                .orElse(false);
    }
}
