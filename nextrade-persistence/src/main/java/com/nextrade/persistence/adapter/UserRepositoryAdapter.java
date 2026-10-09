package com.nextrade.persistence.adapter;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.domain.user.User;
import com.nextrade.domain.user.UserRole;
import com.nextrade.domain.user.UserStatus;
import com.nextrade.persistence.entity.UserJpaEntity;
import com.nextrade.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findById(Identifier.UserId id) {
        return jpaRepository.findByUuid(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return jpaRepository.findByUsernameOrEmail(usernameOrEmail).map(this::toDomain);
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return jpaRepository.findByRole(role.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<User> findByStatus(com.nextrade.domain.user.UserStatus status) {
        return jpaRepository.findByStatus(status.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public User save(User user) {
        return toDomain(jpaRepository.save(toEntity(user)));
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void incrementFailedAttempts(Identifier.UserId userId) {
        jpaRepository.incrementFailedAttempts(userId.getValue());
    }

    @Override
    public void resetFailedAttempts(Identifier.UserId userId) {
        jpaRepository.resetFailedAttempts(userId.getValue());
    }

    @Override
    public void setLockedUntil(Identifier.UserId userId, java.time.Instant lockedUntil) {
        jpaRepository.setLockedUntil(userId.getValue(), lockedUntil);
    }

    private User toDomain(UserJpaEntity e) {
        return User.reconstruct(
            Identifier.UserId.of(e.getUuid()),
            e.getUsername(),
            e.getEmail(),
            e.getPasswordHash(),
            UserRole.valueOf(e.getRole()),
            UserStatus.valueOf(e.getStatus()),
            e.isEmailVerified(),
            e.getFailedAttempts(),
            e.getLockedUntil(),
            e.getCreatedAt(),
            e.getUpdatedAt(),
            e.getVersion()
        );
    }

    private UserJpaEntity toEntity(User user) {
        UserJpaEntity e = new UserJpaEntity();
        e.setUuid(user.getId().getValue());
        e.setUsername(user.getUsername());
        e.setEmail(user.getEmail());
        e.setPasswordHash(user.getPasswordHash());
        e.setRole(user.getRole().name());
        e.setStatus(user.getStatus().name());
        e.setEmailVerified(user.isEmailVerified());
        e.setFailedAttempts(user.getFailedAttempts());
        e.setLockedUntil(user.getLockedUntil());
        return e;
    }
}
