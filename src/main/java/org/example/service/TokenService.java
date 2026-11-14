package org.example.service;

import io.jsonwebtoken.Claims;
import org.example.dto.TokenPair;
import org.example.exception.InvalidTokenException;
import org.example.exception.ValidationException;
import org.example.model.SessionStatus;
import org.example.model.User;
import org.example.model.UserSession;
import org.example.model.UserStatus;
import org.example.repository.UserRepository;
import org.example.repository.UserSessionRepository;
import org.example.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TokenService {

    private final JwtService jwtService;
    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public TokenService(JwtService jwtService, UserSessionRepository sessionRepository,
                        UserRepository userRepository) {
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TokenPair issue(User user, String deviceId) {
        if (deviceId != null && deviceId.length() > 100) {
            throw new ValidationException("deviceId: не длиннее 100 символов");
        }
        JwtService.Token access = jwtService.createAccessToken(user.getUsername());
        JwtService.Token refresh = jwtService.createRefreshToken(user.getUsername());

        UserSession session = new UserSession();
        session.setUsername(user.getUsername());
        session.setDeviceId(deviceId);
        session.setRefreshToken(refresh.value());
        session.setAccessTokenExpiry(access.expiresAt());
        session.setRefreshTokenExpiry(refresh.expiresAt());
        session.setStatus(SessionStatus.ACTIVE);
        sessionRepository.saveAndFlush(session);

        return new TokenPair(access.value(), refresh.value());
    }

    @Transactional(noRollbackFor = InvalidTokenException.class)
    public TokenPair refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidationException("Не указан refreshToken");
        }
        Claims claims = jwtService.validateRefreshToken(refreshToken);
        UserSession session = sessionRepository.findByRefreshTokenForUpdate(refreshToken)
                .filter(found -> found.getRefreshToken().equals(refreshToken))
                .orElseThrow(() -> new InvalidTokenException("Сессия не найдена"));

        boolean notExpired = session.getRefreshTokenExpiry().isAfter(Instant.now());
        if (session.getStatus() == SessionStatus.USED && notExpired) {
            session.setStatus(SessionStatus.REVOKED);
            throw new InvalidTokenException("Refresh-токен уже использован, сессия отозвана");
        }
        if (session.getStatus() != SessionStatus.ACTIVE || !notExpired
                || !session.getUsername().equals(claims.getSubject())) {
            throw new InvalidTokenException("Сессия недействительна");
        }
        User user = userRepository.findByUsernameIgnoreCase(session.getUsername())
                .filter(found -> found.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new InvalidTokenException("Пользователь не найден или заблокирован"));

        session.setStatus(SessionStatus.USED);
        return issue(user, session.getDeviceId());
    }
}
