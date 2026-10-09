package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByUuid(UUID uuid);
    Optional<UserJpaEntity> findByUsername(String username);
    Optional<UserJpaEntity> findByEmail(String email);
    @Query("SELECT u FROM UserJpaEntity u WHERE LOWER(u.username) = LOWER(:usernameOrEmail) OR LOWER(u.email) = LOWER(:usernameOrEmail)")
    Optional<UserJpaEntity> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    List<UserJpaEntity> findByRole(String role);
    List<UserJpaEntity> findByStatus(String status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying @Transactional
    @Query(value = "UPDATE app_user SET failed_attempts = failed_attempts + 1, locked_until = CASE WHEN failed_attempts + 1 >= :maxAttempts THEN CURRENT_TIMESTAMP + make_interval(mins => :lockoutMinutes) ELSE locked_until END WHERE uuid = :userId", nativeQuery = true)
    int incrementFailedAttempts(@Param("userId") UUID userId, @Param("maxAttempts") int maxAttempts, @Param("lockoutMinutes") int lockoutMinutes);

    @Modifying @Transactional
    @Query("UPDATE UserJpaEntity u SET u.failedAttempts = 0, u.lockedUntil = null WHERE u.uuid = :userId")
    void resetFailedAttempts(@Param("userId") UUID userId);

    @Modifying @Transactional
    @Query("UPDATE UserJpaEntity u SET u.lockedUntil = :lockedUntil WHERE u.uuid = :userId")
    void setLockedUntil(@Param("userId") UUID userId, @Param("lockedUntil") java.time.Instant lockedUntil);
}
