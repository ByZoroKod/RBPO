package org.example.service;

import org.example.dto.ListForAuctionRequest;
import org.example.dto.LotClosingResult;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.model.AuctionDeal;
import org.example.model.AuctionLot;
import org.example.model.Bid;
import org.example.model.CollectibleItem;
import org.example.model.DealStatus;
import org.example.model.LotStatus;
import org.example.repository.AuctionDealRepository;
import org.example.repository.AuctionLotRepository;
import org.example.repository.BidRepository;
import org.example.repository.CollectibleItemRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class AuctionService {

    private final CollectibleItemRepository itemRepository;
    private final UserRepository userRepository;
    private final AuctionLotRepository lotRepository;
    private final BidRepository bidRepository;
    private final AuctionDealRepository dealRepository;

    public AuctionService(CollectibleItemRepository itemRepository,
                          UserRepository userRepository,
                          AuctionLotRepository lotRepository,
                          BidRepository bidRepository,
                          AuctionDealRepository dealRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.lotRepository = lotRepository;
        this.bidRepository = bidRepository;
        this.dealRepository = dealRepository;
    }

    public AuctionLot listForAuction(Long itemId, ListForAuctionRequest request) {
        CollectibleItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден: id=" + itemId));

        MoneyValidator.requirePositive(request.getStartPrice(), "Стартовая цена должна быть положительной");
        MoneyValidator.requirePositive(request.getStep(), "Шаг ставки должен быть положительным");
        Instant now = Instant.now();
        if (request.getEndTime() == null || !request.getEndTime().isAfter(now)) {
            throw new ValidationException("Время окончания торгов должно быть в будущем");
        }

        if (lotRepository.existsByItemIdAndStatus(item.getId(), LotStatus.ACTIVE)) {
            throw new ConflictException("Предмет уже выставлен на торги");
        }
        if (dealRepository.existsByItemIdAndStatus(item.getId(), DealStatus.PENDING)) {
            throw new ConflictException("Предмет продан и ожидает передачи новому владельцу");
        }

        AuctionLot lot = new AuctionLot();
        lot.setItemId(item.getId());
        lot.setStartPrice(request.getStartPrice());
        lot.setStep(request.getStep());
        lot.setStartTime(now);
        lot.setEndTime(request.getEndTime());
        lot.setStatus(LotStatus.ACTIVE);
        return lotRepository.save(lot);
    }

    public Bid placeBid(Long lotId, Long bidderId, BigDecimal amount) {
        if (bidderId == null) {
            throw new ValidationException("Не указан участник (bidderId)");
        }
        MoneyValidator.requirePositive(amount, "Сумма ставки должна быть положительной");

        AuctionLot lot = lotRepository.findByIdForUpdate(lotId)
                .orElseThrow(() -> new NotFoundException("Лот не найден: id=" + lotId));
        if (!userRepository.existsById(bidderId)) {
            throw new ValidationException("Участник не найден: id=" + bidderId);
        }

        if (lot.getStatus() != LotStatus.ACTIVE) {
            throw new ConflictException("Ставки принимаются только по активному лоту");
        }
        Instant now = Instant.now();
        if (now.isBefore(lot.getStartTime())) {
            throw new ConflictException("Торги по лоту ещё не начались");
        }
        if (!now.isBefore(lot.getEndTime())) {
            throw new ConflictException("Время торгов по лоту истекло");
        }

        CollectibleItem item = itemRepository.findById(lot.getItemId())
                .orElseThrow(() -> new NotFoundException("Предмет лота не найден: id=" + lot.getItemId()));
        if (item.getOwnerId().equals(bidderId)) {
            throw new ConflictException("Владелец не может делать ставки на свой предмет");
        }

        Optional<Bid> leading = bidRepository.findFirstByLotIdOrderByAmountDescIdAsc(lotId);
        BigDecimal minAmount = lot.getStartPrice();
        if (leading.isPresent()) {
            if (leading.get().getBidderId().equals(bidderId)) {
                throw new ConflictException("Ваша ставка уже лидирует");
            }
            minAmount = leading.get().getAmount().add(lot.getStep());
        }
        if (amount.compareTo(minAmount) < 0) {
            throw new ConflictException("Ставка должна быть не меньше " + minAmount.toPlainString());
        }

        Bid bid = new Bid();
        bid.setLotId(lot.getId());
        bid.setBidderId(bidderId);
        bid.setAmount(amount);
        bid.setTime(now);
        return bidRepository.save(bid);
    }

    public LotClosingResult closeLot(Long lotId) {
        AuctionLot lot = lotRepository.findByIdForUpdate(lotId)
                .orElseThrow(() -> new NotFoundException("Лот не найден: id=" + lotId));
        if (lot.getStatus() != LotStatus.ACTIVE) {
            throw new ConflictException("Завершить можно только активный лот");
        }
        if (dealRepository.existsByLotId(lotId)) {
            throw new ConflictException("По лоту уже есть сделка");
        }

        Optional<Bid> winning = bidRepository.findFirstByLotIdOrderByAmountDescIdAsc(lotId);
        if (winning.isEmpty()) {
            lot.setStatus(LotStatus.CANCELLED);
            return new LotClosingResult(lotRepository.save(lot), null);
        }

        CollectibleItem item = itemRepository.findById(lot.getItemId())
                .orElseThrow(() -> new NotFoundException("Предмет лота не найден: id=" + lot.getItemId()));

        AuctionDeal deal = new AuctionDeal();
        deal.setLotId(lot.getId());
        deal.setSellerId(item.getOwnerId());
        deal.setWinningBidId(winning.get().getId());
        deal.setAmount(winning.get().getAmount());
        deal.setTransferStatus(DealStatus.PENDING);
        deal = dealRepository.save(deal);

        lot.setStatus(LotStatus.FINISHED);
        return new LotClosingResult(lotRepository.save(lot), deal);
    }

    public AuctionDeal confirmTransfer(Long dealId) {
        AuctionDeal deal = dealRepository.findByIdForUpdate(dealId)
                .orElseThrow(() -> new NotFoundException("Сделка не найдена: id=" + dealId));
        if (deal.getTransferStatus() != DealStatus.PENDING) {
            throw new ConflictException("Подтвердить передачу можно только для сделки в статусе PENDING");
        }

        Bid winningBid = bidRepository.findById(deal.getWinningBidId())
                .orElseThrow(() -> new NotFoundException("Выигравшая ставка не найдена: id=" + deal.getWinningBidId()));
        AuctionLot lot = lotRepository.findById(deal.getLotId())
                .orElseThrow(() -> new NotFoundException("Лот не найден: id=" + deal.getLotId()));
        CollectibleItem item = itemRepository.findById(lot.getItemId())
                .orElseThrow(() -> new NotFoundException("Предмет не найден: id=" + lot.getItemId()));

        item.setOwnerId(winningBid.getBidderId());
        itemRepository.save(item);

        deal.setTransferStatus(DealStatus.TRANSFERRED);
        return dealRepository.save(deal);
    }

    public AuctionDeal cancelDeal(Long dealId) {
        AuctionDeal deal = dealRepository.findByIdForUpdate(dealId)
                .orElseThrow(() -> new NotFoundException("Сделка не найдена: id=" + dealId));
        if (deal.getTransferStatus() != DealStatus.PENDING) {
            throw new ConflictException("Отменить можно только сделку в статусе PENDING");
        }

        AuctionLot lot = lotRepository.findByIdForUpdate(deal.getLotId())
                .orElseThrow(() -> new NotFoundException("Лот не найден: id=" + deal.getLotId()));
        lot.setStatus(LotStatus.CANCELLED);
        lotRepository.save(lot);

        deal.setTransferStatus(DealStatus.CANCELLED);
        return dealRepository.save(deal);
    }
}
