package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "collectible_items")
public class CollectibleItem extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "item_condition", length = 100)
    private String condition;
}
