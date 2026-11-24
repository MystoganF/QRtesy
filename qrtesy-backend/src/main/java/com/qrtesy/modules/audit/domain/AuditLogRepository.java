package com.qrtesy.modules.audit.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    @Query("""
            SELECT a FROM AuditLogEntity a
            WHERE (:eventId IS NULL OR a.eventId = :eventId)
            ORDER BY a.createdAt DESC
            """)
    List<AuditLogEntity> findRecent(@Param("eventId") UUID eventId, Pageable pageable);
}