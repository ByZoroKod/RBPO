package org.example.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("jwt")
public record JwtProperties(String secret, Duration accessTtl, Duration refreshTtl) {
}
