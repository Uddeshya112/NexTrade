package com.nextrade.persistence.adapter;

import com.nextrade.common.enumtype.UserRole;
import com.nextrade.common.enumtype.UserStatus;
import com.nextrade.common.identifier.UserId;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.domain.user.User;
import com.nextrade.persistence.entity.UserJpaEntity;
import com.nextrade.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@Transactional
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        return jpaRepository.findByUuid(id.value()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = jpaRepository.findByUuid(user.id().value()).orElseGet(UserJpaEntity::new);
        entity.setUuid(user.id().value());
        entity.setUsername(user.username().value());
        entity.setEmail(user.email().value());
        entity.setPasswordHash(user.passwordHash());
        entity.setRole(user.role().name());
        entity.setStatus(user.status().name());
        entity.setEmailVerified(user.emailVerified());
        entity.setFailedAttempts(user.failedAttempts());
        entity.setLockedUntil(user.lockedUntil());
        entity.setSuspensionReason(user.lockReason());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    private User toDomain(UserJpaEntity entity) {
        Instant createdAt = entity.getCreatedAt() == null ? Instant.now() : entity.getCreatedAt();
        Instant updatedAt = entity.getUpdatedAt() == null ? createdAt : entity.getUpdatedAt();
        return new User(
                com.nextrade.common.identifier.UserId.of(entity.getUuid()),
                entity.getUsername(), entity.getEmail(), entity.getPasswordHash(),
                UserRole.valueOf(entity.getRole()), UserStatus.valueOf(entity.getStatus()),
                entity.isEmailVerified(), entity.getFailedAttempts(), entity.getLockedUntil(),
                createdAt, updatedAt, entity.getVersion() == null ? 0L : entity.getVersion());
    }
}
