package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {
    Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying @Transactional
    @Query("UPDATE PasswordResetTokenJpaEntity t SET t.used = true, t.usedAt = CURRENT_TIMESTAMP WHERE t.tokenHash = :tokenHash AND t.used = false AND t.expiresAt > CURRENT_TIMESTAMP")
    int markUsed(@Param("tokenHash") String tokenHash);

    @Transactional
    default Optional<PasswordResetTokenJpaEntity> consumeValidToken(String tokenDigest) {
        Optional<PasswordResetTokenJpaEntity> tokenOpt = findByTokenHash(tokenDigest);
        if (tokenOpt.isEmpty()) return Optional.empty();
        PasswordResetTokenJpaEntity token = tokenOpt.get();
        if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) return Optional.empty();
        token.markUsed();
        save(token);
        return Optional.of(token);
    }
}
