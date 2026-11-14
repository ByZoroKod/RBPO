package org.example.repository;

import org.example.model.CollectibleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectibleItemRepository extends JpaRepository<CollectibleItem, Long> {
}
