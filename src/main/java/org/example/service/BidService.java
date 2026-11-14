package org.example.service;

import org.example.dto.BidRequest;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.model.Bid;
import org.example.repository.AuctionLotRepository;
import org.example.repository.BidRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionLotRepository lotRepository;
    private final UserRepository userRepository;
    private final AuctionService auctionService;

    public BidService(BidRepository bidRepository,
                      AuctionLotRepository lotRepository,
                      UserRepository userRepository,
                      AuctionService auctionService) {
        this.bidRepository = bidRepository;
        this.lotRepository = lotRepository;
        this.userRepository = userRepository;
        this.auctionService = auctionService;
    }

    @Transactional(readOnly = true)
    public List<Bid> findAll() {
        return bidRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Bid getById(Long id) {
        return bidRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ставка не найдена: id=" + id));
    }

    public Bid create(BidRequest request) {
        if (request.getLotId() == null) {
            throw new ValidationException("Не указан лот (lotId)");
        }
        return auctionService.placeBid(request.getLotId(), request.getBidderId(), request.getAmount());
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
            MoneyValidator.requirePositive(request.getAmount(), "Сумма ставки должна быть положительной");
            bid.setAmount(request.getAmount());
        }
        return bidRepository.save(bid);
    }

    public void delete(Long id) {
        if (!bidRepository.existsById(id)) {
            throw new NotFoundException("Ставка не найдена: id=" + id);
        }
        bidRepository.deleteById(id);
    }
}
