package com.qrtesy.modules.events.features.manage;

import com.qrtesy.modules.audit.domain.AuditAction;
import com.qrtesy.modules.audit.domain.AuditLogService;
import com.qrtesy.modules.events.domain.EventAvailabilityProjection;
import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.events.domain.EventStatus;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import com.qrtesy.modules.registrations.domain.WaitlistPromotionService;
import com.qrtesy.shared.exception.DomainException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManageEventsHandler {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final WaitlistPromotionService waitlistPromotionService;
    private final AuditLogService auditLogService;
    private final ZoneId manilaZoneId;

    public ManageEventsHandler(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            WaitlistPromotionService waitlistPromotionService,
            AuditLogService auditLogService,
            ZoneId manilaZoneId
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.waitlistPromotionService = waitlistPromotionService;
        this.auditLogService = auditLogService;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional(readOnly = true)
    public List<StaffEventResponse> listAll() {
        return eventRepository.findAllWithAvailability().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StaffEventResponse create(SaveEventRequest request) {
        validateTimeRange(request);

        Instant startsAt = ZonedDateTime.of(request.eventDate(), request.startTime(), manilaZoneId).toInstant();
        Instant endsAt = ZonedDateTime.of(request.eventDate(), request.endTime(), manilaZoneId).toInstant();
        Instant now = Instant.now();

        EventEntity saved = eventRepository.save(
                EventEntity.create(
                        request.title().trim(),
                        request.description().trim(),
                        request.venue().trim(),
                        startsAt,
                        endsAt,
                        request.capacity(),
                        request.status(),
                        now
                )
        );

        auditLogService.logCurrentStaffAction(
                saved.getId(),
                null,
                AuditAction.EVENT_CREATED,
                "Created event \"" + saved.getTitle() + "\" (" + saved.getStatus().name()
                        + ", capacity " + saved.getCapacity() + ")."
        );

        return toResponse(saved, 0L, 0L);
    }

    @Transactional
    public StaffEventResponse update(UUID eventId, SaveEventRequest request) {
        validateTimeRange(request);

        EventEntity event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found."
                ));

        if (event.getStatus() == EventStatus.CLOSED) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "EVENT_CLOSED",
                    "Closed events cannot be modified."
            );
        }

        long occupiedBefore = registrationRepository.countOccupiedSpots(eventId);
        long waitlistBefore = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.WAITLISTED);

        if (request.status() == EventStatus.DRAFT && (occupiedBefore > 0 || waitlistBefore > 0)) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "CANNOT_UNPUBLISH_WITH_REGISTRATIONS",
                    "Events with active registrations cannot be reverted to Draft. Close the event instead."
            );
        }

        if (request.capacity() < occupiedBefore) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "CAPACITY_BELOW_REGISTERED",
                    "Capacity cannot be lower than the number of people already registered (" + occupiedBefore + ")."
            );
        }

        EventStatus previousStatus = event.getStatus();
        int previousCapacity = event.getCapacity();

        Instant startsAt = ZonedDateTime.of(request.eventDate(), request.startTime(), manilaZoneId).toInstant();
        Instant endsAt = ZonedDateTime.of(request.eventDate(), request.endTime(), manilaZoneId).toInstant();

        event.update(
                request.title().trim(),
                request.description().trim(),
                request.venue().trim(),
                startsAt,
                endsAt,
                request.capacity(),
                request.status(),
                Instant.now()
        );

        EventEntity saved = eventRepository.save(event);

        if (saved.getStatus() == EventStatus.CLOSED && previousStatus != EventStatus.CLOSED) {
            auditLogService.logCurrentStaffAction(
                    saved.getId(),
                    null,
                    AuditAction.EVENT_CLOSED,
                    "Closed event \"" + saved.getTitle() + "\"."
            );
        } else {
            auditLogService.logCurrentStaffAction(
                    saved.getId(),
                    null,
                    AuditAction.EVENT_UPDATED,
                    "Updated event \"" + saved.getTitle() + "\" (status: " + saved.getStatus().name()
                            + ", capacity: " + previousCapacity + " -> " + saved.getCapacity() + ")."
            );
        }

        if (saved.getStatus() == EventStatus.PUBLISHED) {
            waitlistPromotionService.promoteFromWaitlist(eventId, saved.getCapacity());
        }

        long occupiedAfter = registrationRepository.countOccupiedSpots(eventId);
        long waitlistAfter = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.WAITLISTED);
        return toResponse(saved, occupiedAfter, waitlistAfter);
    }

    @Transactional
    public StaffEventResponse close(UUID eventId) {
        EventEntity event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found."
                ));

        if (event.getStatus() == EventStatus.CLOSED) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "EVENT_ALREADY_CLOSED",
                    "This event is already closed."
            );
        }

        event.close(Instant.now());
        EventEntity saved = eventRepository.save(event);

        auditLogService.logCurrentStaffAction(
                saved.getId(),
                null,
                AuditAction.EVENT_CLOSED,
                "Closed event \"" + saved.getTitle() + "\"."
        );

        long occupiedCount = registrationRepository.countOccupiedSpots(eventId);
        long waitlistCount = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.WAITLISTED);
        return toResponse(saved, occupiedCount, waitlistCount);
    }

    private void validateTimeRange(SaveEventRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new DomainException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_TIME_RANGE",
                    "End time must be after start time."
            );
        }
    }

    private StaffEventResponse toResponse(EventAvailabilityProjection p) {
        ZonedDateTime startManila = p.getStartsAt().atZone(manilaZoneId);
        ZonedDateTime endManila = p.getEndsAt().atZone(manilaZoneId);
        long remaining = Math.max(0L, p.getCapacity() - p.getOccupiedCount());
        long waitlistCount = registrationRepository.countByEventIdAndStatus(
                p.getId(), RegistrationStatus.WAITLISTED
        );

        return new StaffEventResponse(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getVenue(),
                startManila.toLocalDate(),
                startManila.toLocalTime(),
                endManila.toLocalTime(),
                startManila.toOffsetDateTime(),
                endManila.toOffsetDateTime(),
                manilaZoneId.getId(),
                p.getCapacity(),
                p.getOccupiedCount(),
                remaining,
                waitlistCount,
                p.getStatus()
        );
    }

    private StaffEventResponse toResponse(EventEntity e, long occupiedCount, long waitlistCount) {
        ZonedDateTime startManila = e.getStartsAt().atZone(manilaZoneId);
        ZonedDateTime endManila = e.getEndsAt().atZone(manilaZoneId);
        long remaining = Math.max(0L, e.getCapacity() - occupiedCount);

        return new StaffEventResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getVenue(),
                startManila.toLocalDate(),
                startManila.toLocalTime(),
                endManila.toLocalTime(),
                startManila.toOffsetDateTime(),
                endManila.toOffsetDateTime(),
                manilaZoneId.getId(),
                e.getCapacity(),
                occupiedCount,
                remaining,
                waitlistCount,
                e.getStatus().name()
        );
    }
}