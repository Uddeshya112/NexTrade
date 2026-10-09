package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByUuid(UUID uuid);
    Optional<UserJpaEntity> findByUsername(String username);
    Optional<UserJpaEntity> findByEmail(String email);
    Optional<UserJpaEntity> findByUsernameOrEmail(String usernameOrEmail);
    List<UserJpaEntity> findByRole(String role);
    List<UserJpaEntity> findByStatus(String status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying @Transactional
    @Query("UPDATE UserJpaEntity u SET u.failedAttempts = u.failedAttempts + 1, u.lockedUntil = CASE WHEN u.failedAttempts + 1 >= :maxAttempts THEN CURRENT_TIMESTAMP + (:lockoutMinutes * 60) * INTERVAL '1 SECOND' ELSE u.lockedUntil END WHERE u.uuid = :userId")
    int incrementFailedAttempts(@Param("userId") UUID userId, @Param("maxAttempts") int maxAttempts, @Param("lockoutMinutes") int lockoutMinutes);

    @Modifying @Transactional
    @Query("UPDATE UserJpaEntity u SET u.failedAttempts = 0, u.lockedUntil = null WHERE u.uuid = :userId")
    void resetFailedAttempts(@Param("userId") UUID userId);

    @Modifying @Transactional
    @Query("UPDATE UserJpaEntity u SET u.lockedUntil = :lockedUntil WHERE u.uuid = :userId")
    void setLockedUntil(@Param("userId") UUID userId, @Param("lockedUntil") java.time.Instant lockedUntil);
}
