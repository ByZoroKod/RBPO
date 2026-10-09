package org.example.controller;

import org.example.dto.CollectibleItemRequest;
import org.example.model.CollectibleItem;
import org.example.service.CollectibleItemService;
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
@RequestMapping("/api/collectible-items")
public class CollectibleItemController {

    private final CollectibleItemService itemService;

    public CollectibleItemController(CollectibleItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<CollectibleItem> findAll() {
        return itemService.findAll();
    }

    @GetMapping("/{id}")
    public CollectibleItem findById(@PathVariable Long id) {
        return itemService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CollectibleItem> create(@RequestBody CollectibleItemRequest request) {
        CollectibleItem created = itemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public CollectibleItem update(@PathVariable Long id, @RequestBody CollectibleItemRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }
}

