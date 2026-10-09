package org.example.service;

import org.example.dto.AuctionDealRequest;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.model.AuctionDeal;
import org.example.model.AuctionLot;
import org.example.model.Bid;
import org.example.model.DealStatus;
import org.example.model.LotStatus;
import org.example.repository.AuctionDealRepository;
import org.example.repository.AuctionLotRepository;
import org.example.repository.BidRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionDealService {

    private final AuctionDealRepository dealRepository;
    private final AuctionLotRepository lotRepository;
    private final BidRepository bidRepository;

    public AuctionDealService(AuctionDealRepository dealRepository,
                              AuctionLotRepository lotRepository,
                              BidRepository bidRepository) {
        this.dealRepository = dealRepository;
        this.lotRepository = lotRepository;
        this.bidRepository = bidRepository;
    }

    public List<AuctionDeal> findAll() {
        return dealRepository.findAll();
    }

    public AuctionDeal getById(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Сделка не найдена: id=" + id));
    }

    public AuctionDeal create(AuctionDealRequest request) {
        if (request.getLotId() == null) {
            throw new ValidationException("Не указан лот (lotId)");
        }
        if (request.getWinningBidId() == null) {
            throw new ValidationException("Не указана выигравшая ставка (winningBidId)");
        }
        AuctionLot lot = lotRepository.findById(request.getLotId())
                .orElseThrow(() -> new ValidationException("Лот не найден: id=" + request.getLotId()));
        Bid winningBid = bidRepository.findById(request.getWinningBidId())
                .orElseThrow(() -> new ValidationException(
                        "Ставка не найдена: id=" + request.getWinningBidId()));

        if (!lot.getId().equals(winningBid.getLotId())) {
            throw new ValidationException("Выигравшая ставка относится к другому лоту");
        }
        if (!dealRepository.findByLotId(lot.getId()).isEmpty()) {
            throw new ValidationException("По лоту уже есть сделка: lotId=" + lot.getId());
        }

        AuctionDeal deal = new AuctionDeal();
        deal.setLotId(lot.getId());
        deal.setWinningBidId(winningBid.getId());
        deal.setAmount(winningBid.getAmount());
        deal.setTransferStatus(request.getTransferStatus() != null
                ? request.getTransferStatus()
                : DealStatus.PENDING);
        AuctionDeal saved = dealRepository.save(deal);

        lot.setStatus(LotStatus.FINISHED);
        lotRepository.save(lot);

        return saved;
    }

    public AuctionDeal update(Long id, AuctionDealRequest request) {
        AuctionDeal deal = getById(id);

        if (request.getLotId() != null) {
            AuctionLot lot = lotRepository.findById(request.getLotId())
                    .orElseThrow(() -> new ValidationException("Лот не найден: id=" + request.getLotId()));
            deal.setLotId(lot.getId());
        }
        if (request.getWinningBidId() != null) {
            Bid winningBid = bidRepository.findById(request.getWinningBidId())
                    .orElseThrow(() -> new ValidationException(
                            "Ставка не найдена: id=" + request.getWinningBidId()));
            if (!deal.getLotId().equals(winningBid.getLotId())) {
                throw new ValidationException("Выигравшая ставка относится к другому лоту");
            }
            deal.setWinningBidId(winningBid.getId());
            deal.setAmount(winningBid.getAmount());
        }
        if (request.getTransferStatus() != null) {
            deal.setTransferStatus(request.getTransferStatus());
        }
        return dealRepository.save(deal);
    }

    public void delete(Long id) {
        if (!dealRepository.existsById(id)) {
            throw new NotFoundException("Сделка не найдена: id=" + id);
        }
        dealRepository.deleteById(id);
    }

    public void deleteByLot(Long lotId) {
        for (AuctionDeal deal : dealRepository.findByLotId(lotId)) {
            dealRepository.deleteById(deal.getId());
        }
    }

    public void deleteByWinningBid(Long bidId) {
        for (AuctionDeal deal : dealRepository.findByWinningBidId(bidId)) {
            dealRepository.deleteById(deal.getId());
        }
    }
}
