package org.example.service;

import org.example.dto.AuctionDealRequest;
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
import org.example.security.AppUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuctionDealService {

    private final AuctionDealRepository dealRepository;
    private final AuctionLotRepository lotRepository;
    private final BidRepository bidRepository;
    private final CollectibleItemRepository itemRepository;

    public AuctionDealService(AuctionDealRepository dealRepository,
                              AuctionLotRepository lotRepository,
                              BidRepository bidRepository,
                              CollectibleItemRepository itemRepository) {
        this.dealRepository = dealRepository;
        this.lotRepository = lotRepository;
        this.bidRepository = bidRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<AuctionDeal> findAllFor(AppUserDetails actor) {
        if (actor.isAdmin()) {
            return dealRepository.findAll();
        }
        return dealRepository.findByParticipant(actor.getId());
    }

    @Transactional(readOnly = true)
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
        AuctionLot lot = lotRepository.findByIdForUpdate(request.getLotId())
                .orElseThrow(() -> new ValidationException("Лот не найден: id=" + request.getLotId()));
        Bid winningBid = bidRepository.findById(request.getWinningBidId())
                .orElseThrow(() -> new ValidationException("Ставка не найдена: id=" + request.getWinningBidId()));

        if (!lot.getId().equals(winningBid.getLotId())) {
            throw new ValidationException("Выигравшая ставка относится к другому лоту");
        }
        if (dealRepository.existsByLotId(lot.getId())) {
            throw new ConflictException("По лоту уже есть сделка: lotId=" + lot.getId());
        }

        CollectibleItem item = itemRepository.findById(lot.getItemId())
                .orElseThrow(() -> new ValidationException("Предмет лота не найден: id=" + lot.getItemId()));

        AuctionDeal deal = new AuctionDeal();
        deal.setLotId(lot.getId());
        deal.setSellerId(item.getOwnerId());
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

        Long lotId = request.getLotId() != null ? request.getLotId() : deal.getLotId();
        Long bidId = request.getWinningBidId() != null ? request.getWinningBidId() : deal.getWinningBidId();
        if (!lotId.equals(deal.getLotId()) || !bidId.equals(deal.getWinningBidId())) {
            if (!lotRepository.existsById(lotId)) {
                throw new ValidationException("Лот не найден: id=" + lotId);
            }
            Bid winningBid = bidRepository.findById(bidId)
                    .orElseThrow(() -> new ValidationException("Ставка не найдена: id=" + bidId));
            if (!lotId.equals(winningBid.getLotId())) {
                throw new ValidationException("Выигравшая ставка относится к другому лоту");
            }
            deal.setLotId(lotId);
            deal.setWinningBidId(bidId);
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
}
