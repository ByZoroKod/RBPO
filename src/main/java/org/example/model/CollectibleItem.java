package org.example.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectibleItem extends BaseEntity {
    private String description;
    private Long ownerId;
    private String condition;
}
