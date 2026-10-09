package org.example.controller;

import org.example.dto.AuctionDealRequest;
import org.example.model.AuctionDeal;
import org.example.service.AuctionDealService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auction-deals")
public class AuctionDealController {

    private final AuctionDealService dealService;

    public AuctionDealController(AuctionDealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping
    public List<AuctionDeal> findAll() {
        return dealService.findAll();
    }

    @GetMapping("/{id}")
    public AuctionDeal findById(@PathVariable Long id) {
        return dealService.getById(id);
    }

    @PostMapping
    public ResponseEntity<AuctionDeal> create(@RequestBody AuctionDealRequest request) {
        AuctionDeal created = dealService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public AuctionDeal update(@PathVariable Long id, @RequestBody AuctionDealRequest request) {
        return dealService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dealService.delete(id);
    }
}

