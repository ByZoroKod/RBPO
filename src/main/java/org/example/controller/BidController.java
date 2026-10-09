package org.example.controller;

import org.example.dto.BidRequest;
import org.example.model.Bid;
import org.example.service.BidService;
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
@RequestMapping("/api/bids")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @GetMapping
    public List<Bid> findAll() {
        return bidService.findAll();
    }

    @GetMapping("/{id}")
    public Bid findById(@PathVariable Long id) {
        return bidService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Bid> create(@RequestBody BidRequest request) {
        Bid created = bidService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public Bid update(@PathVariable Long id, @RequestBody BidRequest request) {
        return bidService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bidService.delete(id);
    }
}

