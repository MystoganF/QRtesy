package com.qrtesy.modules.events.features.manage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StaffEventResponse(
        UUID id,
        String title,
        String description,
        String venue,
        LocalDate eventDate,
        LocalTime startTime,
        LocalTime endTime,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String timezone,
        int capacity,
        long occupiedCount,
        long remainingSpots,
        long waitlistCount,
        String status
) {
}