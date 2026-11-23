package com.qrtesy.modules.events.features.listpublic;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PublicEventSummaryResponse(
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
        boolean full
) {
}