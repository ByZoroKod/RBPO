package org.example.service;

import org.example.dto.AuctionLotRequest;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.model.AuctionLot;
import org.example.model.LotStatus;
import org.example.repository.AuctionLotRepository;
import org.example.repository.CollectibleItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AuctionLotService {

    private final AuctionLotRepository lotRepository;
    private final CollectibleItemRepository itemRepository;

    public AuctionLotService(AuctionLotRepository lotRepository, CollectibleItemRepository itemRepository) {
        this.lotRepository = lotRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<AuctionLot> findAll() {
        return lotRepository.findAll();
    }

    @Transactional(readOnly = true)
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
        MoneyValidator.requirePositive(request.getStartPrice(), "Стартовая цена должна быть положительной");
        MoneyValidator.requirePositive(request.getStep(), "Шаг ставки должен быть положительным");
        if (request.getEndTime() == null) {
            throw new ValidationException("Не указано время окончания торгов (endTime)");
        }

        AuctionLot lot = new AuctionLot();
        lot.setItemId(request.getItemId());
        lot.setStartPrice(request.getStartPrice());
        lot.setStep(request.getStep());
        lot.setStartTime(request.getStartTime() != null ? request.getStartTime() : Instant.now());
        lot.setEndTime(request.getEndTime());
        lot.setStatus(request.getStatus() != null ? request.getStatus() : LotStatus.ACTIVE);
        validatePeriod(lot);
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
            MoneyValidator.requirePositive(request.getStartPrice(), "Стартовая цена должна быть положительной");
            lot.setStartPrice(request.getStartPrice());
        }
        if (request.getStep() != null) {
            MoneyValidator.requirePositive(request.getStep(), "Шаг ставки должен быть положительным");
            lot.setStep(request.getStep());
        }
        if (request.getStartTime() != null) {
            lot.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            lot.setEndTime(request.getEndTime());
        }
        if (request.getStatus() != null) {
            lot.setStatus(request.getStatus());
        }
        validatePeriod(lot);
        return lotRepository.save(lot);
    }

    public void delete(Long id) {
        if (!lotRepository.existsById(id)) {
            throw new NotFoundException("Лот не найден: id=" + id);
        }
        lotRepository.deleteById(id);
    }

    private void validatePeriod(AuctionLot lot) {
        if (!lot.getEndTime().isAfter(lot.getStartTime())) {
            throw new ValidationException("Время окончания должно быть позже времени начала");
        }
    }
}
