package com.qrtesy.modules.registrations.features.staffattendees;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record EventAttendeesResponse(
        UUID eventId,
        String eventTitle,
        String eventVenue,
        OffsetDateTime eventStartsAt,
        OffsetDateTime eventEndsAt,
        String timezone,
        int capacity,
        int confirmedCount,
        int checkedInCount,
        int totalOccupied,
        int remainingSpots,
        int waitlistedCount,
        List<AttendeeItemResponse> attendees
) {
}