package com.qrtesy.modules.events.features.getdetails;

import com.qrtesy.modules.events.domain.EventAvailabilityProjection;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.shared.exception.DomainException;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetEventDetailsHandler {

    private final EventRepository eventRepository;
    private final ZoneId manilaZoneId;

    public GetEventDetailsHandler(EventRepository eventRepository, ZoneId manilaZoneId) {
        this.eventRepository = eventRepository;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional(readOnly = true)
    public EventDetailsResponse handle(UUID eventId) {
        EventAvailabilityProjection row = eventRepository.findPublishedByIdWithAvailability(eventId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found or is no longer available."
                ));

        int capacity = row.getCapacity();
        int occupied = row.getOccupiedCount().intValue();
        int remaining = Math.max(0, capacity - occupied);

        return new EventDetailsResponse(
                row.getId(),
                row.getTitle(),
                row.getDescription(),
                row.getVenue(),
                row.getStartsAt().atZone(manilaZoneId).toOffsetDateTime(),
                row.getEndsAt().atZone(manilaZoneId).toOffsetDateTime(),
                manilaZoneId.getId(),
                capacity,
                occupied,
                remaining,
                remaining == 0,
                row.getStatus()
        );
    }
}