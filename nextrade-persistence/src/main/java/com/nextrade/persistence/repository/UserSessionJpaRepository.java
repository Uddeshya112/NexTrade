package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.UserSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionJpaEntity, UUID> {
    
    @Modifying @Transactional
    @Query("UPDATE UserSessionJpaEntity s SET s.revoked = true WHERE s.id = :id")
    void revokeById(@Param("id") UUID id);

    @Modifying @Transactional
    @Query("UPDATE UserSessionJpaEntity s SET s.revoked = true WHERE s.userId = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);

    @Modifying @Transactional
    @Query("UPDATE UserSessionJpaEntity s SET s.revoked = true, s.rotatedToSessionId = :newSessionId WHERE s.id = :id")
    void markRotated(@Param("id") UUID id, @Param("newSessionId") UUID newSessionId);

    // ATOMIC: Check digest AND revoke in single query
    @Modifying @Transactional
    @Query("""
        UPDATE UserSessionJpaEntity s 
        SET s.revoked = true 
        WHERE s.id = :sessionId 
        AND s.revoked = false 
        AND s.expiresAt > CURRENT_TIMESTAMP
        AND s.refreshTokenDigest = :providedDigest
    """)
    int revokeAndValidateDigest(@Param("sessionId") UUID sessionId, @Param("providedDigest") String providedDigest);
}
