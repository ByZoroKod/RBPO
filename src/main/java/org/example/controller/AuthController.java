package org.example.controller;

import org.example.dto.LoginRequest;
import org.example.dto.RefreshRequest;
import org.example.dto.RegisterRequest;
import org.example.dto.TokenPair;
import org.example.service.AuthService;
import org.example.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public TokenPair login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenPair register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/refresh")
    public TokenPair refresh(@RequestBody RefreshRequest request) {
        return tokenService.refresh(request.refreshToken());
    }
}
