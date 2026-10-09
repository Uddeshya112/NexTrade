package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    List<OutboxEventEntity> findUnpublished(int limit);

    @Modifying @Transactional
    @Query("UPDATE OutboxEventEntity e SET e.published = true, e.publishedAt = CURRENT_TIMESTAMP WHERE e.id IN :ids")
    void markPublished(@Param("ids") List<UUID> ids);
}
