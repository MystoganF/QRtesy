package com.qrtesy.modules.events.features.dashboard;

import java.util.List;

public record StaffDashboardResponse(
        int totalEvents,
        int publishedEvents,
        int draftEvents,
        int closedEvents,
        int totalCapacity,
        int totalOccupied,
        int totalCheckedIn,
        int totalNoShows,
        int totalWaitlisted,
        double overallCheckInRatePercent,
        List<DashboardEventSummary> events
) {
}