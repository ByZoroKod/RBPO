package org.example.service;

import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.dto.TokenPair;
import org.example.exception.ValidationException;
import org.example.model.User;
import org.example.security.AppUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;

    public AuthService(AuthenticationManager authenticationManager, UserService userService,
                       TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    public TokenPair login(LoginRequest request) {
        if (isBlank(request.username()) || isBlank(request.password())) {
            throw new ValidationException("Не указаны логин или пароль");
        }
        AppUserDetails principal = (AppUserDetails) authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()))
                .getPrincipal();
        return tokenService.issue(principal.getUser(), request.deviceId());
    }

    @Transactional
    public TokenPair register(RegisterRequest request) {
        User user = userService.register(request);
        return tokenService.issue(user, request.getDeviceId());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
