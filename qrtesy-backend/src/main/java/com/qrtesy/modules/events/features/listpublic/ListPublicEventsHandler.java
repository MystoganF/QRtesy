package com.qrtesy.modules.events.features.listpublic;

import com.qrtesy.modules.events.domain.EventAvailabilityProjection;
import com.qrtesy.modules.events.domain.EventRepository;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPublicEventsHandler {

    private final EventRepository eventRepository;
    private final ZoneId manilaZoneId;

    public ListPublicEventsHandler(EventRepository eventRepository, ZoneId manilaZoneId) {
        this.eventRepository = eventRepository;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional(readOnly = true)
    public List<PublicEventSummaryResponse> handle() {
        return eventRepository.findAllPublishedWithAvailability()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PublicEventSummaryResponse toResponse(EventAvailabilityProjection row) {
        int capacity = row.getCapacity();
        int occupied = row.getOccupiedCount().intValue();
        int remaining = Math.max(0, capacity - occupied);

        return new PublicEventSummaryResponse(
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
                remaining == 0
        );
    }
}