package com.nextrade.persistence.adapter;

import com.nextrade.common.identifier.UserId;
import com.nextrade.domain.repository.SupabaseIdentityRepository;
import com.nextrade.persistence.entity.SupabaseIdentityEntity;
import com.nextrade.persistence.repository.SupabaseIdentityJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class SupabaseIdentityRepositoryAdapter implements SupabaseIdentityRepository {
    private final SupabaseIdentityJpaRepository repository;

    public SupabaseIdentityRepositoryAdapter(SupabaseIdentityJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserId> findUserId(UUID supabaseUserId) {
        return repository.findById(supabaseUserId).map(entity -> UserId.of(entity.getNextradeUserId()));
    }

    @Override
    public void link(UUID supabaseUserId, UserId nextradeUserId) {
        if (repository.existsById(supabaseUserId)) return;
        repository.save(new SupabaseIdentityEntity(supabaseUserId, nextradeUserId.value()));
    }
}
