package com.qrtesy.modules.registrations.features.staffattendees;

import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import com.qrtesy.shared.exception.DomainException;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListEventAttendeesHandler {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final ZoneId manilaZoneId;

    public ListEventAttendeesHandler(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            ZoneId manilaZoneId
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional(readOnly = true)
    public EventAttendeesResponse handle(UUID eventId, String rawQuery) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found."
                ));

        String searchPattern = (rawQuery == null || rawQuery.isBlank())
                ? "%"
                : "%" + rawQuery.trim().toLowerCase(Locale.ROOT) + "%";

        List<AttendeeItemResponse> attendees = registrationRepository.searchByEventId(eventId, searchPattern)
                .stream()
                .map(this::toItem)
                .toList();

        int confirmedCount = (int) registrationRepository.countByEventIdAndStatus(
                eventId, RegistrationStatus.CONFIRMED
        );
        int checkedInCount = (int) registrationRepository.countByEventIdAndStatus(
                eventId, RegistrationStatus.CHECKED_IN
        );
        int waitlistedCount = (int) registrationRepository.countByEventIdAndStatus(
                eventId, RegistrationStatus.WAITLISTED
        );
        int totalOccupied = confirmedCount + checkedInCount;
        int remainingSpots = Math.max(0, event.getCapacity() - totalOccupied);

        return new EventAttendeesResponse(
                event.getId(),
                event.getTitle(),
                event.getVenue(),
                event.getStartsAt().atZone(manilaZoneId).toOffsetDateTime(),
                event.getEndsAt().atZone(manilaZoneId).toOffsetDateTime(),
                manilaZoneId.getId(),
                event.getCapacity(),
                confirmedCount,
                checkedInCount,
                totalOccupied,
                remainingSpots,
                waitlistedCount,
                attendees
        );
    }

    private AttendeeItemResponse toItem(RegistrationEntity r) {
        Integer waitlistPosition = r.getStatus() == RegistrationStatus.WAITLISTED
                ? registrationRepository.findWaitlistPosition(r.getEventId(), r.getRegisteredAt(), r.getId())
                : null;

        return new AttendeeItemResponse(
                r.getId(),
                r.getTicketCode(),
                r.getFullName(),
                r.getEmail(),
                r.getMobile(),
                r.getStatus().name(),
                r.getRegisteredAt().atZone(manilaZoneId).toOffsetDateTime(),
                r.getCheckedInAt() != null
                        ? r.getCheckedInAt().atZone(manilaZoneId).toOffsetDateTime()
                        : null,
                waitlistPosition
        );
    }
}