package com.qrtesy.modules.registrations.features.promote;

import com.qrtesy.modules.audit.domain.AuditAction;
import com.qrtesy.modules.audit.domain.AuditLogService;
import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.events.domain.EventStatus;
import com.qrtesy.modules.notifications.domain.EmailNotificationService;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import com.qrtesy.modules.registrations.features.register.RegistrationResponse;
import com.qrtesy.shared.exception.DomainException;
import com.qrtesy.shared.security.TicketQrSigner;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromoteWaitlistHandler {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final TicketQrSigner ticketQrSigner;
    private final AuditLogService auditLogService;
    private final EmailNotificationService emailNotificationService;
    private final ZoneId manilaZoneId;

    public PromoteWaitlistHandler(
            RegistrationRepository registrationRepository,
            EventRepository eventRepository,
            TicketQrSigner ticketQrSigner,
            AuditLogService auditLogService,
            EmailNotificationService emailNotificationService,
            ZoneId manilaZoneId
    ) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.ticketQrSigner = ticketQrSigner;
        this.auditLogService = auditLogService;
        this.emailNotificationService = emailNotificationService;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional
    public RegistrationResponse promote(UUID registrationId) {
        RegistrationEntity registration = registrationRepository.findByIdForUpdate(registrationId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "REGISTRATION_NOT_FOUND",
                        "Registration was not found."
                ));

        if (registration.getStatus() != RegistrationStatus.WAITLISTED) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "NOT_WAITLISTED",
                    "Only waitlisted registrations can be promoted."
            );
        }

        EventEntity event = eventRepository.findByIdForUpdate(registration.getEventId())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found."
                ));

        if (event.getStatus() == EventStatus.CLOSED) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "EVENT_CLOSED",
                    "Cannot promote registrations for a closed event."
            );
        }

        long occupied = registrationRepository.countOccupiedSpots(event.getId());
        if (occupied >= event.getCapacity()) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "EVENT_FULL",
                    "No open seats available. Cancel a confirmed registration or increase capacity first."
            );
        }

        int position = registrationRepository.findWaitlistPosition(
                event.getId(),
                registration.getRegisteredAt(),
                registration.getId()
        );
        if (position > 1) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "NOT_FIRST_IN_WAITLIST",
                    "Only the #1 person in the waitlist queue can be promoted next."
            );
        }

        registration.promoteToConfirmed();
        RegistrationEntity saved = registrationRepository.save(registration);

        auditLogService.logCurrentStaffAction(
                event.getId(),
                saved.getId(),
                AuditAction.WAITLIST_PROMOTED,
                "Promoted " + saved.getFullName() + " (" + saved.getTicketCode() + ") from waitlist to Confirmed."
        );

        emailNotificationService.sendWaitlistPromotionAfterCommit(event, saved);

        String qrToken = ticketQrSigner.sign(event.getId(), saved.getTicketCode());

        return new RegistrationResponse(
                saved.getId(),
                saved.getTicketCode(),
                qrToken,
                saved.getFullName(),
                saved.getEmail(),
                saved.getMobile(),
                saved.getStatus().name(),
                saved.getRegisteredAt().atZone(manilaZoneId).toOffsetDateTime(),
                null,
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