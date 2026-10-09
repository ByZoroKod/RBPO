package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectibleItemRequest {
    private String description;
    private Long ownerId;
    private String condition;
}
