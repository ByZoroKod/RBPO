package org.example.service;

import org.example.dto.CollectibleItemRequest;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.model.CollectibleItem;
import org.example.model.DealStatus;
import org.example.model.LotStatus;
import org.example.repository.AuctionDealRepository;
import org.example.repository.AuctionLotRepository;
import org.example.repository.CollectibleItemRepository;
import org.example.repository.UserRepository;
import org.example.security.AppUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CollectibleItemService {

    private final CollectibleItemRepository itemRepository;
    private final UserRepository userRepository;
    private final AuctionLotRepository lotRepository;
    private final AuctionDealRepository dealRepository;

    public CollectibleItemService(CollectibleItemRepository itemRepository,
                                  UserRepository userRepository,
                                  AuctionLotRepository lotRepository,
                                  AuctionDealRepository dealRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.lotRepository = lotRepository;
        this.dealRepository = dealRepository;
    }

    @Transactional(readOnly = true)
    public List<CollectibleItem> findAll() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CollectibleItem getById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Предмет не найден: id=" + id));
    }

    public CollectibleItem create(CollectibleItemRequest request) {
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new ValidationException("Не указано описание предмета");
        }
        if (request.getOwnerId() == null) {
            throw new ValidationException("Не указан владелец (ownerId)");
        }
        if (!userRepository.existsById(request.getOwnerId())) {
            throw new ValidationException("Владелец не найден: id=" + request.getOwnerId());
        }
        CollectibleItem item = new CollectibleItem();
        item.setDescription(request.getDescription());
        item.setOwnerId(request.getOwnerId());
        item.setCondition(request.getCondition());
        return itemRepository.save(item);
    }

    public CollectibleItem update(Long id, CollectibleItemRequest request, AppUserDetails actor) {
        CollectibleItem item = getById(id);
        requireNotInAuctionUnlessAdmin(item, actor);
        if (request.getDescription() != null) {
            if (request.getDescription().isBlank()) {
                throw new ValidationException("Описание не может быть пустым");
            }
            item.setDescription(request.getDescription());
        }
        if (request.getOwnerId() != null) {
            if (!userRepository.existsById(request.getOwnerId())) {
                throw new ValidationException("Владелец не найден: id=" + request.getOwnerId());
            }
            item.setOwnerId(request.getOwnerId());
        }
        if (request.getCondition() != null) {
            item.setCondition(request.getCondition());
        }
        return itemRepository.save(item);
    }

    public void delete(Long id, AppUserDetails actor) {
        CollectibleItem item = getById(id);
        requireNotInAuctionUnlessAdmin(item, actor);
        itemRepository.deleteById(id);
    }

    private void requireNotInAuctionUnlessAdmin(CollectibleItem item, AppUserDetails actor) {
        if (actor.isAdmin()) {
            return;
        }
        if (lotRepository.existsByItemIdAndStatus(item.getId(), LotStatus.ACTIVE)
                || dealRepository.existsByItemIdAndStatus(item.getId(), DealStatus.PENDING)) {
            throw new ConflictException("Предмет участвует в торгах или ждёт передачи: изменение и удаление недоступны");
        }
    }
}
