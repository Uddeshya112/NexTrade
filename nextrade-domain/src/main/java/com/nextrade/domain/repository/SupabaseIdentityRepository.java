package com.nextrade.domain.repository;

import com.nextrade.common.identifier.UserId;

import java.util.Optional;
import java.util.UUID;

public interface SupabaseIdentityRepository {
    Optional<UserId> findUserId(UUID supabaseUserId);
    void link(UUID supabaseUserId, UserId nextradeUserId);
}
