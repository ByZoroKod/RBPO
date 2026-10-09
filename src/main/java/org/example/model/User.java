package org.example.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends BaseEntity {
    private String name;
    private String email;
}
