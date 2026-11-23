package com.qrtesy.modules.events.features.getdetails;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventDetailsResponse(
        UUID id,
        String title,
        String description,
        String venue,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String timezone,
        int capacity,
        int confirmedCount,
        int remainingSpots,
        boolean full,
        String status
) {
}