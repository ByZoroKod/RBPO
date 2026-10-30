package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.model.Role;
import org.example.model.UserStatus;

@Getter
@Setter
public class UserRequest {
    private String name;
    private String email;
    private String username;
    private String password;
    private Role role;
    private UserStatus status;
}
