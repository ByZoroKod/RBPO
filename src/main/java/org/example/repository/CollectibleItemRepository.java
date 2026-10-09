package org.example.repository;

import org.example.model.CollectibleItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CollectibleItemRepository extends InMemoryRepository<CollectibleItem> {

    public List<CollectibleItem> findByOwnerId(Long ownerId) {
        return findBy(item -> ownerId != null && ownerId.equals(item.getOwnerId()));
    }
}

