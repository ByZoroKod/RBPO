package org.example.service;

import org.example.dto.AuctionLotRequest;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.model.AuctionLot;
import org.example.model.LotStatus;
import org.example.repository.AuctionLotRepository;
import org.example.repository.CollectibleItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionLotService {

    private final AuctionLotRepository lotRepository;
    private final CollectibleItemRepository itemRepository;
    private final BidService bidService;
    private final AuctionDealService auctionDealService;

    public AuctionLotService(AuctionLotRepository lotRepository,
                             CollectibleItemRepository itemRepository,
                             BidService bidService,
                             AuctionDealService auctionDealService) {
        this.lotRepository = lotRepository;
        this.itemRepository = itemRepository;
        this.bidService = bidService;
        this.auctionDealService = auctionDealService;
    }

    public List<AuctionLot> findAll() {
        return lotRepository.findAll();
    }

    public AuctionLot getById(Long id) {
        return lotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Лот не найден: id=" + id));
    }

    public AuctionLot create(AuctionLotRequest request) {
        if (request.getItemId() == null) {
            throw new ValidationException("Не указан предмет (itemId)");
        }
        if (!itemRepository.existsById(request.getItemId())) {
            throw new ValidationException("Предмет не найден: id=" + request.getItemId());
        }
        if (request.getStartPrice() == null || request.getStartPrice().signum() <= 0) {
            throw new ValidationException("Стартовая цена должна быть положительной");
        }
        if (request.getStep() == null || request.getStep().signum() <= 0) {
            throw new ValidationException("Шаг ставки должен быть положительным");
        }
        validatePeriod(request);

        AuctionLot lot = new AuctionLot();
        lot.setItemId(request.getItemId());
        lot.setStartPrice(request.getStartPrice());
        lot.setStep(request.getStep());
        lot.setStartTime(request.getStartTime());
        lot.setEndTime(request.getEndTime());
        lot.setStatus(request.getStatus() != null ? request.getStatus() : LotStatus.ACTIVE);
        return lotRepository.save(lot);
    }

    public AuctionLot update(Long id, AuctionLotRequest request) {
        AuctionLot lot = getById(id);

        if (request.getItemId() != null) {
            if (!itemRepository.existsById(request.getItemId())) {
                throw new ValidationException("Предмет не найден: id=" + request.getItemId());
            }
            lot.setItemId(request.getItemId());
        }
        if (request.getStartPrice() != null) {
            if (request.getStartPrice().signum() <= 0) {
                throw new ValidationException("Стартовая цена должна быть положительной");
            }
            lot.setStartPrice(request.getStartPrice());
        }
        if (request.getStep() != null) {
            if (request.getStep().signum() <= 0) {
                throw new ValidationException("Шаг ставки должен быть положительным");
            }
            lot.setStep(request.getStep());
        }
        if (request.getStartTime() != null) {
            lot.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            lot.setEndTime(request.getEndTime());
        }
        if (lot.getStartTime() != null && lot.getEndTime() != null
                && lot.getEndTime().isBefore(lot.getStartTime())) {
            throw new ValidationException("Дата окончания раньше даты начала");
        }
        if (request.getStatus() != null) {
            lot.setStatus(request.getStatus());
        }
        return lotRepository.save(lot);
    }

    public void delete(Long id) {
        if (!lotRepository.existsById(id)) {
            throw new NotFoundException("Лот не найден: id=" + id);
        }
        bidService.deleteByLot(id);
        auctionDealService.deleteByLot(id);
        lotRepository.deleteById(id);
    }

    public void deleteByItem(Long itemId) {
        for (AuctionLot lot : lotRepository.findByItemId(itemId)) {
            delete(lot.getId());
        }
    }

    private void validatePeriod(AuctionLotRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
                && request.getEndTime().isBefore(request.getStartTime())) {
            throw new ValidationException("Дата окончания раньше даты начала");
        }
    }
}
