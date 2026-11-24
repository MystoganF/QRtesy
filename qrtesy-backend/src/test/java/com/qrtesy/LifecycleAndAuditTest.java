package com.qrtesy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qrtesy.modules.audit.domain.AuditAction;
import com.qrtesy.modules.audit.domain.AuditLogService;
import com.qrtesy.modules.events.domain.DashboardEventProjection;
import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.events.domain.EventStatus;
import com.qrtesy.modules.events.features.dashboard.StaffDashboardHandler;
import com.qrtesy.modules.events.features.dashboard.StaffDashboardResponse;
import com.qrtesy.modules.events.features.manage.ManageEventsHandler;
import com.qrtesy.modules.events.features.manage.SaveEventRequest;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.WaitlistPromotionService;
import com.qrtesy.shared.exception.DomainException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class LifecycleAndAuditTest {

    @Test
    void closedEventCannotBeEditedOrClosedTwice() {
        EventRepository eventRepository = mock(EventRepository.class);
        RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
        WaitlistPromotionService waitlistService = mock(WaitlistPromotionService.class);
        AuditLogService auditLogService = mock(AuditLogService.class);

        UUID eventId = UUID.randomUUID();
        EventEntity closedEvent = EventEntity.create(
                "Closed Summit",
                "Description",
                "Cebu",
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().plus(2, ChronoUnit.HOURS),
                50,
                EventStatus.CLOSED,
                Instant.now().minus(1, ChronoUnit.DAYS)
        );
        ReflectionTestUtils.setField(closedEvent, "id", eventId);

        when(eventRepository.findByIdForUpdate(eventId)).thenReturn(Optional.of(closedEvent));

        ManageEventsHandler handler = new ManageEventsHandler(
                eventRepository,
                registrationRepository,
                waitlistService,
                auditLogService,
                ZoneId.of("Asia/Manila")
        );

        SaveEventRequest updateRequest = new SaveEventRequest(
                "Updated Title",
                "Updated Desc",
                "Cebu",
                LocalDate.now(),
                LocalTime.of(13, 0),
                LocalTime.of(17, 0),
                60,
                EventStatus.PUBLISHED
        );

        DomainException editEx = assertThrows(DomainException.class, () -> handler.update(eventId, updateRequest));
        assertEquals("EVENT_CLOSED", editEx.getCode());

        DomainException closeEx = assertThrows(DomainException.class, () -> handler.close(eventId));
        assertEquals("EVENT_ALREADY_CLOSED", closeEx.getCode());
    }

    @Test
    void closingPublishedEventWritesAuditLog() {
        EventRepository eventRepository = mock(EventRepository.class);
        RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
        WaitlistPromotionService waitlistService = mock(WaitlistPromotionService.class);
        AuditLogService auditLogService = mock(AuditLogService.class);

        UUID eventId = UUID.randomUUID();
        EventEntity publishedEvent = EventEntity.create(
                "Tech Meetup",
                "Description",
                "Cebu",
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(2, ChronoUnit.HOURS),
                30,
                EventStatus.PUBLISHED,
                Instant.now().minus(1, ChronoUnit.DAYS)
        );
        ReflectionTestUtils.setField(publishedEvent, "id", eventId);

        when(eventRepository.findByIdForUpdate(eventId)).thenReturn(Optional.of(publishedEvent));
        when(eventRepository.save(any(EventEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ManageEventsHandler handler = new ManageEventsHandler(
                eventRepository,
                registrationRepository,
                waitlistService,
                auditLogService,
                ZoneId.of("Asia/Manila")
        );

        handler.close(eventId);

        assertEquals(EventStatus.CLOSED, publishedEvent.getStatus());
        verify(auditLogService).logCurrentStaffAction(
                eq(eventId),
                eq(null),
                eq(AuditAction.EVENT_CLOSED),
                any(String.class)
        );
    }

    @Test
    void dashboardComputesPostStartNoShowsOnlyForStartedEvents() {
        EventRepository eventRepository = mock(EventRepository.class);

        DashboardEventProjection startedEvent = mock(DashboardEventProjection.class);
        when(startedEvent.getId()).thenReturn(UUID.randomUUID());
        when(startedEvent.getTitle()).thenReturn("Started Meetup");
        when(startedEvent.getVenue()).thenReturn("Cebu");
        when(startedEvent.getStartsAt()).thenReturn(Instant.now().minus(30, ChronoUnit.MINUTES));
        when(startedEvent.getEndsAt()).thenReturn(Instant.now().plus(2, ChronoUnit.HOURS));
        when(startedEvent.getCapacity()).thenReturn(20);
        when(startedEvent.getStatus()).thenReturn("PUBLISHED");
        when(startedEvent.getConfirmedCount()).thenReturn(4L);
        when(startedEvent.getCheckedInCount()).thenReturn(11L);
        when(startedEvent.getWaitlistCount()).thenReturn(0L);
        when(startedEvent.getCancelledCount()).thenReturn(1L);

        DashboardEventProjection futureEvent = mock(DashboardEventProjection.class);
        when(futureEvent.getId()).thenReturn(UUID.randomUUID());
        when(futureEvent.getTitle()).thenReturn("Upcoming Clinic");
        when(futureEvent.getVenue()).thenReturn("IT Park");
        when(futureEvent.getStartsAt()).thenReturn(Instant.now().plus(2, ChronoUnit.DAYS));
        when(futureEvent.getEndsAt()).thenReturn(Instant.now().plus(2, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS));
        when(futureEvent.getCapacity()).thenReturn(10);
        when(futureEvent.getStatus()).thenReturn("PUBLISHED");
        when(futureEvent.getConfirmedCount()).thenReturn(6L);
        when(futureEvent.getCheckedInCount()).thenReturn(0L);
        when(futureEvent.getWaitlistCount()).thenReturn(0L);
        when(futureEvent.getCancelledCount()).thenReturn(0L);

        when(eventRepository.findAllDashboardSummaries()).thenReturn(List.of(startedEvent, futureEvent));

        StaffDashboardHandler handler = new StaffDashboardHandler(
                eventRepository,
                ZoneId.of("Asia/Manila")
        );

        StaffDashboardResponse response = handler.handle();

        assertEquals(4, response.totalNoShows());
        assertTrue(response.events().get(0).hasStarted());
        assertEquals(4, response.events().get(0).noShowCount());
        assertFalse(response.events().get(1).hasStarted());
        assertEquals(0, response.events().get(1).noShowCount());
    }
}