package org.example.repository;

import org.example.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    @Query(value = "select * from user_sessions where refresh_token_hash = sha2(:token, 256) for update",
            nativeQuery = true)
    Optional<UserSession> findByRefreshTokenForUpdate(@Param("token") String token);
}
