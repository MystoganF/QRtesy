package com.qrtesy.modules.events.features.dashboard;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DashboardEventSummary(
        UUID id,
        String title,
        String venue,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String timezone,
        String status,
        int capacity,
        int confirmedCount,
        int checkedInCount,
        int occupiedCount,
        int remainingSpots,
        int waitlistCount,
        int cancelledCount,
        double checkInRatePercent
) {
}