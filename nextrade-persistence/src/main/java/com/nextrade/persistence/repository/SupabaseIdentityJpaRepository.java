package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.SupabaseIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupabaseIdentityJpaRepository extends JpaRepository<SupabaseIdentityEntity, UUID> { }
