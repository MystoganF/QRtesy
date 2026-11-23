package com.qrtesy.modules.events.features.manage;

import com.qrtesy.modules.events.domain.EventAvailabilityProjection;
import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
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
    private final ZoneId manilaZoneId;

    public ManageEventsHandler(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            ZoneId manilaZoneId
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
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

        return toResponse(saved, 0L);
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

        long occupiedCount = registrationRepository.countOccupiedSpots(eventId);
        if (request.capacity() < occupiedCount) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "CAPACITY_BELOW_REGISTERED",
                    "Capacity cannot be lower than the number of people already registered (" + occupiedCount + ")."
            );
        }

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
        return toResponse(saved, occupiedCount);
    }

    @Transactional
    public StaffEventResponse close(UUID eventId) {
        EventEntity event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found."
                ));

        event.close(Instant.now());
        EventEntity saved = eventRepository.save(event);
        long occupiedCount = registrationRepository.countOccupiedSpots(eventId);
        return toResponse(saved, occupiedCount);
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
                p.getStatus()
        );
    }

    private StaffEventResponse toResponse(EventEntity e, long occupiedCount) {
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
                e.getStatus().name()
        );
    }
}