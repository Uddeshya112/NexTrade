package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.EmailVerificationTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationTokenJpaEntity, UUID> {
    Optional<EmailVerificationTokenJpaEntity> findBySelectorAndCodeHash(String selector, String codeHash);
    Optional<EmailVerificationTokenJpaEntity> findByCodeHash(String codeHash);

    @Modifying @Transactional
    @Query("UPDATE EmailVerificationTokenJpaEntity t SET t.used = true, t.usedAt = CURRENT_TIMESTAMP, t.attemptCount = t.attemptCount + 1 WHERE t.id = :id AND t.codeHash = :codeHash AND t.used = false AND t.expiresAt > CURRENT_TIMESTAMP AND t.attemptCount < :maxAttempts")
    int markUsedIfValid(@Param("id") UUID id, @Param("codeHash") String codeHash, @Param("maxAttempts") int maxAttempts);

    @Modifying @Transactional
    @Query("UPDATE EmailVerificationTokenJpaEntity t SET t.used = true, t.usedAt = CURRENT_TIMESTAMP WHERE t.userId = :userId AND t.used = false")
    void invalidatePrevious(@Param("userId") UUID userId);

    long countRecentResends(UUID userId, java.time.Duration window);
}
