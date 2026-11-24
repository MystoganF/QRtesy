package com.qrtesy.modules.registrations.features.cancel;

import com.qrtesy.modules.audit.domain.AuditAction;
import com.qrtesy.modules.audit.domain.AuditLogService;
import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.events.domain.EventStatus;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import com.qrtesy.modules.registrations.domain.WaitlistPromotionService;
import com.qrtesy.modules.registrations.features.register.RegistrationResponse;
import com.qrtesy.shared.exception.DomainException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelRegistrationHandler {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final WaitlistPromotionService waitlistPromotionService;
    private final AuditLogService auditLogService;
    private final ZoneId manilaZoneId;

    public CancelRegistrationHandler(
            RegistrationRepository registrationRepository,
            EventRepository eventRepository,
            WaitlistPromotionService waitlistPromotionService,
            AuditLogService auditLogService,
            ZoneId manilaZoneId
    ) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.waitlistPromotionService = waitlistPromotionService;
        this.auditLogService = auditLogService;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional
    public RegistrationResponse cancelByAttendee(String ticketCode, String email) {
        String cleanCode = ticketCode.trim().toUpperCase(Locale.ROOT);
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);

        RegistrationEntity registration = registrationRepository.findByTicketCodeForUpdate(cleanCode)
                .filter(r -> r.getEmail().trim().toLowerCase(Locale.ROOT).equals(cleanEmail))
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "TICKET_NOT_FOUND",
                        "Ticket not found"
                ));

        return executeCancellation(registration, false);
    }

    @Transactional
    public RegistrationResponse cancelByStaff(UUID registrationId) {
        RegistrationEntity registration = registrationRepository.findByIdForUpdate(registrationId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "REGISTRATION_NOT_FOUND",
                        "Registration was not found."
                ));

        return executeCancellation(registration, true);
    }

    private RegistrationResponse executeCancellation(RegistrationEntity registration, boolean initiatedByStaff) {
        if (registration.getStatus() == RegistrationStatus.CHECKED_IN) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "ALREADY_CHECKED_IN",
                    "Checked-in registrations cannot be cancelled."
            );
        }

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "ALREADY_CANCELLED",
                    "This registration has already been cancelled."
            );
        }

        EventEntity event = eventRepository.findByIdForUpdate(registration.getEventId())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found."
                ));

        if (!initiatedByStaff) {
            if (event.getStatus() == EventStatus.CLOSED) {
                throw new DomainException(
                        HttpStatus.CONFLICT,
                        "EVENT_CLOSED",
                        "Registrations can no longer be cancelled because this event is closed."
                );
            }
            if (event.getEndsAt().isBefore(Instant.now())) {
                throw new DomainException(
                        HttpStatus.CONFLICT,
                        "EVENT_ENDED",
                        "Registrations can no longer be cancelled after the event has ended."
                );
            }
        }

        boolean freedConfirmedSpot = registration.getStatus() == RegistrationStatus.CONFIRMED;
        registration.cancel();
        RegistrationEntity saved = registrationRepository.save(registration);

        String auditSummary = "Cancelled registration for " + saved.getFullName()
                + " (" + saved.getTicketCode() + ").";

        if (initiatedByStaff) {
            auditLogService.logCurrentStaffAction(
                    event.getId(),
                    saved.getId(),
                    AuditAction.REGISTRATION_CANCELLED,
                    auditSummary
            );
        } else {
            auditLogService.logAttendeeAction(
                    saved.getEmail(),
                    event.getId(),
                    saved.getId(),
                    AuditAction.REGISTRATION_CANCELLED,
                    auditSummary
            );
        }



        return new RegistrationResponse(
                saved.getId(),
                saved.getTicketCode(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getMobile(),
                saved.getStatus().name(),
                saved.getRegisteredAt().atZone(manilaZoneId).toOffsetDateTime(),
                saved.getCheckedInAt() != null
                        ? saved.getCheckedInAt().atZone(manilaZoneId).toOffsetDateTime()
                        : null,
                event.getId(),
                event.getTitle(),
                event.getVenue(),
                event.getStartsAt().atZone(manilaZoneId).toOffsetDateTime(),
                event.getEndsAt().atZone(manilaZoneId).toOffsetDateTime(),
                manilaZoneId.getId(),
                null
        );
    }
}