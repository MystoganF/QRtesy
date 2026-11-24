package com.qrtesy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qrtesy.modules.audit.domain.AuditAction;
import com.qrtesy.modules.audit.domain.AuditLogService;
import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.events.domain.EventStatus;
import com.qrtesy.modules.notifications.domain.EmailNotificationService;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import com.qrtesy.modules.registrations.features.promote.PromoteWaitlistHandler;
import com.qrtesy.modules.registrations.features.register.RegisterAttendeeHandler;
import com.qrtesy.modules.registrations.features.register.RegisterAttendeeRequest;
import com.qrtesy.modules.registrations.features.register.RegistrationResponse;
import com.qrtesy.shared.exception.DomainException;
import com.qrtesy.shared.security.TicketQrSigner;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmailAndPromotionTest {

    @Test
    void registrationSendsConfirmationEmailAfterCommit() {
        EventRepository eventRepository = mock(EventRepository.class);
        RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
        EmailNotificationService emailService = mock(EmailNotificationService.class);
        TicketQrSigner signer = new TicketQrSigner("test-secret-key-at-least-32-characters-long");

        UUID eventId = UUID.randomUUID();
        EventEntity event = EventEntity.create(
                "Cebu Dev Meetup 2026",
                "Community meetup",
                "Ayala Center Cebu",
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS),
                30,
                EventStatus.PUBLISHED,
                Instant.now()
        );

        when(eventRepository.findByIdForUpdate(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsActiveRegistration(eq(event.getId()), any())).thenReturn(false);
        when(registrationRepository.countOccupiedSpots(event.getId())).thenReturn(5L);
        when(registrationRepository.existsByTicketCode(any())).thenReturn(false);
        when(registrationRepository.save(any(RegistrationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterAttendeeHandler handler = new RegisterAttendeeHandler(
                eventRepository,
                registrationRepository,
                signer,
                emailService,
                ZoneId.of("Asia/Manila")
        );

        RegistrationResponse response = handler.handle(new RegisterAttendeeRequest(
                eventId,
                "Juan Dela Cruz",
                "juan@example.com",
                "09171234567"
        ));

        assertEquals("CONFIRMED", response.status());
        verify(emailService).sendRegistrationConfirmationAfterCommit(eq(event), any(RegistrationEntity.class), eq(null));
    }

    @Test
    void manualPromotePromotesFirstWaitlistedAndSendsEmail() {
        RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
        EventRepository eventRepository = mock(EventRepository.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        EmailNotificationService emailService = mock(EmailNotificationService.class);
        TicketQrSigner signer = new TicketQrSigner("test-secret-key-at-least-32-characters-long");

        UUID regId = UUID.randomUUID();
        EventEntity event = EventEntity.create(
                "Cebu Dev Meetup 2026",
                "Community meetup",
                "Ayala Center Cebu",
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS),
                30,
                EventStatus.PUBLISHED,
                Instant.now()
        );

        RegistrationEntity waitlisted = RegistrationEntity.createWaitlisted(
                event.getId(),
                "Maria Clara",
                "maria@example.com",
                "09181234567",
                "QRT-9X2K7M",
                Instant.now()
        );

        when(registrationRepository.findByIdForUpdate(regId)).thenReturn(Optional.of(waitlisted));
        when(eventRepository.findByIdForUpdate(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.countOccupiedSpots(event.getId())).thenReturn(29L);
        when(registrationRepository.findWaitlistPosition(eq(event.getId()), any(), any())).thenReturn(1);
        when(registrationRepository.save(any(RegistrationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PromoteWaitlistHandler handler = new PromoteWaitlistHandler(
                registrationRepository,
                eventRepository,
                signer,
                auditLogService,
                emailService,
                ZoneId.of("Asia/Manila")
        );

        RegistrationResponse result = handler.promote(regId);

        assertEquals("CONFIRMED", result.status());
        assertEquals(RegistrationStatus.CONFIRMED, waitlisted.getStatus());
        verify(auditLogService).logCurrentStaffAction(
                eq(event.getId()),
                eq(waitlisted.getId()),
                eq(AuditAction.WAITLIST_PROMOTED),
                any(String.class)
        );
        verify(emailService).sendWaitlistPromotionAfterCommit(event, waitlisted);
    }

    @Test
    void manualPromoteBlocksNonFirstWaitlistedAttendee() {
        RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
        EventRepository eventRepository = mock(EventRepository.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        EmailNotificationService emailService = mock(EmailNotificationService.class);
        TicketQrSigner signer = new TicketQrSigner("test-secret-key-at-least-32-characters-long");

        UUID regId = UUID.randomUUID();
        EventEntity event = EventEntity.create(
                "Cebu Dev Meetup 2026",
                "Community meetup",
                "Ayala Center Cebu",
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS),
                30,
                EventStatus.PUBLISHED,
                Instant.now()
        );

        RegistrationEntity secondInLine = RegistrationEntity.createWaitlisted(
                event.getId(),
                "Pedro Penduko",
                "pedro@example.com",
                "09191234567",
                "QRT-4B8N2P",
                Instant.now()
        );

        when(registrationRepository.findByIdForUpdate(regId)).thenReturn(Optional.of(secondInLine));
        when(eventRepository.findByIdForUpdate(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.countOccupiedSpots(event.getId())).thenReturn(29L);
        when(registrationRepository.findWaitlistPosition(eq(event.getId()), any(), any())).thenReturn(2);

        PromoteWaitlistHandler handler = new PromoteWaitlistHandler(
                registrationRepository,
                eventRepository,
                signer,
                auditLogService,
                emailService,
                ZoneId.of("Asia/Manila")
        );

        DomainException ex = assertThrows(DomainException.class, () -> handler.promote(regId));
        assertEquals("NOT_FIRST_IN_WAITLIST", ex.getCode());
    }
}