package com.qrtesy.modules.events.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    @Query(value = """
            SELECT
                e.id AS id,
                e.title AS title,
                e.description AS description,
                e.venue AS venue,
                e.starts_at AS startsAt,
                e.ends_at AS endsAt,
                e.capacity AS capacity,
                e.status AS status,
                COALESCE(COUNT(r.id), 0) AS occupiedCount
            FROM qrtesy.events e
            LEFT JOIN qrtesy.registrations r
                ON r.event_id = e.id
               AND r.status IN ('CONFIRMED', 'CHECKED_IN')
            WHERE e.status = 'PUBLISHED'
            GROUP BY e.id, e.title, e.description, e.venue, e.starts_at, e.ends_at, e.capacity, e.status
            ORDER BY e.starts_at ASC
            """, nativeQuery = true)
    List<EventAvailabilityProjection> findAllPublishedWithAvailability();

    @Query(value = """
            SELECT
                e.id AS id,
                e.title AS title,
                e.description AS description,
                e.venue AS venue,
                e.starts_at AS startsAt,
                e.ends_at AS endsAt,
                e.capacity AS capacity,
                e.status AS status,
                COALESCE(COUNT(r.id), 0) AS occupiedCount
            FROM qrtesy.events e
            LEFT JOIN qrtesy.registrations r
                ON r.event_id = e.id
               AND r.status IN ('CONFIRMED', 'CHECKED_IN')
            WHERE e.id = :eventId
              AND e.status = 'PUBLISHED'
            GROUP BY e.id, e.title, e.description, e.venue, e.starts_at, e.ends_at, e.capacity, e.status
            """, nativeQuery = true)
    Optional<EventAvailabilityProjection> findPublishedByIdWithAvailability(@Param("eventId") UUID eventId);
}