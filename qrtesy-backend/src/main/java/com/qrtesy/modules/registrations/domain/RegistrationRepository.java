package com.qrtesy.modules.registrations.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistrationRepository extends JpaRepository<RegistrationEntity, UUID> {

    @Query("""
            SELECT COUNT(r)
            FROM RegistrationEntity r
            WHERE r.eventId = :eventId
              AND r.status IN (com.qrtesy.modules.registrations.domain.RegistrationStatus.CONFIRMED,
                               com.qrtesy.modules.registrations.domain.RegistrationStatus.CHECKED_IN)
            """)
    long countOccupiedSpots(@Param("eventId") UUID eventId);

    @Query("""
            SELECT COUNT(r) > 0
            FROM RegistrationEntity r
            WHERE r.eventId = :eventId
              AND LOWER(r.email) = LOWER(:email)
              AND r.status <> com.qrtesy.modules.registrations.domain.RegistrationStatus.CANCELLED
            """)
    boolean existsActiveRegistration(@Param("eventId") UUID eventId, @Param("email") String email);

    boolean existsByTicketCode(String ticketCode);

    @Query(value = """
            SELECT
                r.id AS registrationId,
                r.ticket_code AS ticketCode,
                r.full_name AS fullName,
                r.email AS email,
                r.mobile AS mobile,
                r.status AS status,
                r.registered_at AS registeredAt,
                r.checked_in_at AS checkedInAt,
                e.id AS eventId,
                e.title AS eventTitle,
                e.venue AS eventVenue,
                e.starts_at AS eventStartsAt,
                e.ends_at AS eventEndsAt
            FROM qrtesy.registrations r
            JOIN qrtesy.events e ON e.id = r.event_id
            WHERE UPPER(r.ticket_code) = UPPER(:ticketCode)
            """, nativeQuery = true)
    Optional<TicketDetailsProjection> findTicketDetailsByCode(@Param("ticketCode") String ticketCode);
}