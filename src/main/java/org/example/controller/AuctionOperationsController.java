package org.example.controller;

import org.example.dto.ListForAuctionRequest;
import org.example.dto.LotClosingResult;
import org.example.dto.PlaceBidRequest;
import org.example.model.AuctionDeal;
import org.example.model.AuctionLot;
import org.example.model.Bid;
import org.example.security.AppUserDetails;
import org.example.service.AuctionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuctionOperationsController {

    private final AuctionService auctionService;

    public AuctionOperationsController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping("/collectible-items/{itemId}/list-for-auction")
    @PreAuthorize("@access.ownsItem(principal.id, #itemId)")
    public ResponseEntity<AuctionLot> listForAuction(@PathVariable Long itemId,
                                                     @RequestBody ListForAuctionRequest request) {
        AuctionLot lot = auctionService.listForAuction(itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lot);
    }

    @PostMapping("/auction-lots/{lotId}/place-bid")
    public ResponseEntity<Bid> placeBid(@PathVariable Long lotId,
                                        @RequestBody PlaceBidRequest request,
                                        @AuthenticationPrincipal AppUserDetails actor) {
        Bid bid = auctionService.placeBid(lotId, actor.getId(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(bid);
    }

    @PostMapping("/auction-lots/{lotId}/close")
    @PreAuthorize("hasRole('ADMIN') or @access.ownsLot(principal.id, #lotId)")
    public LotClosingResult closeLot(@PathVariable Long lotId) {
        return auctionService.closeLot(lotId);
    }

    @PostMapping("/auction-deals/{dealId}/confirm-transfer")
    @PreAuthorize("hasRole('ADMIN') or @access.isDealSeller(principal.id, #dealId)")
    public AuctionDeal confirmTransfer(@PathVariable Long dealId) {
        return auctionService.confirmTransfer(dealId);
    }

    @PostMapping("/auction-deals/{dealId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @access.participatesInDeal(principal.id, #dealId)")
    public AuctionDeal cancelDeal(@PathVariable Long dealId) {
        return auctionService.cancelDeal(dealId);
    }
}
