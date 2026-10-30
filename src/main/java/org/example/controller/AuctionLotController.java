package org.example.controller;

import org.example.dto.AuctionLotRequest;
import org.example.model.AuctionLot;
import org.example.service.AuctionLotService;
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
@RequestMapping("/api/auction-lots")
public class AuctionLotController {

    private final AuctionLotService lotService;

    public AuctionLotController(AuctionLotService lotService) {
        this.lotService = lotService;
    }

    @GetMapping
    public List<AuctionLot> findAll() {
        return lotService.findAll();
    }

    @GetMapping("/{id}")
    public AuctionLot findById(@PathVariable Long id) {
        return lotService.getById(id);
    }

    @PostMapping
    public ResponseEntity<AuctionLot> create(@RequestBody AuctionLotRequest request) {
        AuctionLot created = lotService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public AuctionLot update(@PathVariable Long id, @RequestBody AuctionLotRequest request) {
        return lotService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        lotService.delete(id);
    }
}

