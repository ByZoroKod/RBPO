package org.example.service;

import org.example.dto.BidRequest;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.model.AuctionLot;
import org.example.model.Bid;
import org.example.model.LotStatus;
import org.example.repository.AuctionLotRepository;
import org.example.repository.BidRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionLotRepository lotRepository;
    private final UserRepository userRepository;
    private final AuctionDealService auctionDealService;

    public BidService(BidRepository bidRepository,
                      AuctionLotRepository lotRepository,
                      UserRepository userRepository,
                      AuctionDealService auctionDealService) {
        this.bidRepository = bidRepository;
        this.lotRepository = lotRepository;
        this.userRepository = userRepository;
        this.auctionDealService = auctionDealService;
    }

    public List<Bid> findAll() {
        return bidRepository.findAll();
    }

    public Bid getById(Long id) {
        return bidRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ставка не найдена: id=" + id));
    }

    public Bid create(BidRequest request) {
        if (request.getLotId() == null) {
            throw new ValidationException("Не указан лот (lotId)");
        }
        if (request.getBidderId() == null) {
            throw new ValidationException("Не указан участник (bidderId)");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new ValidationException("Сумма ставки должна быть положительной");
        }
        AuctionLot lot = lotRepository.findById(request.getLotId())
                .orElseThrow(() -> new ValidationException("Лот не найден: id=" + request.getLotId()));
        if (!userRepository.existsById(request.getBidderId())) {
            throw new ValidationException("Участник не найден: id=" + request.getBidderId());
        }
        if (lot.getStatus() != LotStatus.ACTIVE) {
            throw new ValidationException("Ставки принимаются только по активному лоту");
        }

        BigDecimal minRequired = calculateMinBid(lot);
        if (request.getAmount().compareTo(minRequired) < 0) {
            throw new ValidationException("Сумма ставки должна быть не меньше " + minRequired);
        }

        Bid bid = new Bid();
        bid.setLotId(lot.getId());
        bid.setBidderId(request.getBidderId());
        bid.setAmount(request.getAmount());
        bid.setTime(Instant.now());
        return bidRepository.save(bid);
    }

    public Bid update(Long id, BidRequest request) {
        Bid bid = getById(id);

        if (request.getLotId() != null) {
            if (!lotRepository.existsById(request.getLotId())) {
                throw new ValidationException("Лот не найден: id=" + request.getLotId());
            }
            bid.setLotId(request.getLotId());
        }
        if (request.getBidderId() != null) {
            if (!userRepository.existsById(request.getBidderId())) {
                throw new ValidationException("Участник не найден: id=" + request.getBidderId());
            }
            bid.setBidderId(request.getBidderId());
        }
        if (request.getAmount() != null) {
            if (request.getAmount().signum() <= 0) {
                throw new ValidationException("Сумма ставки должна быть положительной");
            }
            bid.setAmount(request.getAmount());
        }
        return bidRepository.save(bid);
    }

    public void delete(Long id) {
        if (!bidRepository.existsById(id)) {
            throw new NotFoundException("Ставка не найдена: id=" + id);
        }
        auctionDealService.deleteByWinningBid(id);
        bidRepository.deleteById(id);
    }

    private BigDecimal calculateMinBid(AuctionLot lot) {
        return bidRepository.findByLotId(lot.getId()).stream()
                .map(Bid::getAmount)
                .max(Comparator.naturalOrder())
                .map(max -> max.add(lot.getStep()))
                .orElse(lot.getStartPrice());
    }

    public void deleteByLot(Long lotId) {
        for (Bid bid : bidRepository.findByLotId(lotId)) {
            auctionDealService.deleteByWinningBid(bid.getId());
            bidRepository.deleteById(bid.getId());
        }
    }

    public void deleteByBidder(Long bidderId) {
        for (Bid bid : bidRepository.findByBidderId(bidderId)) {
            auctionDealService.deleteByWinningBid(bid.getId());
            bidRepository.deleteById(bid.getId());
        }
    }
}
