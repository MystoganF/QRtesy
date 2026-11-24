package com.qrtesy.modules.events.features.dashboard;

import com.qrtesy.modules.events.domain.DashboardEventProjection;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.events.domain.EventStatus;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffDashboardHandler {

    private final EventRepository eventRepository;
    private final ZoneId manilaZoneId;

    public StaffDashboardHandler(
            EventRepository eventRepository,
            ZoneId manilaZoneId
    ) {
        this.eventRepository = eventRepository;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional(readOnly = true)
    public StaffDashboardResponse handle() {
        Instant now = Instant.now();
        List<DashboardEventProjection> rows = eventRepository.findAllDashboardSummaries();

        int publishedEvents = 0;
        int draftEvents = 0;
        int closedEvents = 0;
        int totalCapacity = 0;
        int totalOccupied = 0;
        int totalCheckedIn = 0;
        int totalNoShows = 0;
        int totalWaitlisted = 0;

        List<DashboardEventSummary> summaries = rows.stream()
                .map(row -> toSummary(row, now))
                .toList();

        for (DashboardEventSummary s : summaries) {
            if (EventStatus.PUBLISHED.name().equals(s.status())) {
                publishedEvents++;
            } else if (EventStatus.DRAFT.name().equals(s.status())) {
                draftEvents++;
            } else {
                closedEvents++;
            }
            totalCapacity += s.capacity();
            totalOccupied += s.occupiedCount();
            totalCheckedIn += s.checkedInCount();
            totalNoShows += s.noShowCount();
            totalWaitlisted += s.waitlistCount();
        }

        double overallRate = totalOccupied > 0
                ? Math.round((totalCheckedIn * 1000.0) / totalOccupied) / 10.0
                : 0.0;

        return new StaffDashboardResponse(
                summaries.size(),
                publishedEvents,
                draftEvents,
                closedEvents,
                totalCapacity,
                totalOccupied,
                totalCheckedIn,
                totalNoShows,
                totalWaitlisted,
                overallRate,
                summaries
        );
    }

    private DashboardEventSummary toSummary(DashboardEventProjection row, Instant now) {
        int confirmed = (int) row.getConfirmedCount();
        int checkedIn = (int) row.getCheckedInCount();
        int waitlisted = (int) row.getWaitlistCount();
        int cancelled = (int) row.getCancelledCount();

        boolean hasStarted = !now.isBefore(row.getStartsAt())
                && !EventStatus.DRAFT.name().equals(row.getStatus());
        int noShows = hasStarted ? confirmed : 0;

        int occupied = confirmed + checkedIn;
        int remaining = Math.max(0, row.getCapacity() - occupied);
        double rate = occupied > 0
                ? Math.round((checkedIn * 1000.0) / occupied) / 10.0
                : 0.0;

        return new DashboardEventSummary(
                row.getId(),
                row.getTitle(),
                row.getVenue(),
                row.getStartsAt().atZone(manilaZoneId).toOffsetDateTime(),
                row.getEndsAt().atZone(manilaZoneId).toOffsetDateTime(),
                manilaZoneId.getId(),
                row.getStatus(),
                hasStarted,
                row.getCapacity(),
                confirmed,
                checkedIn,
                noShows,
                occupied,
                remaining,
                waitlisted,
                cancelled,
                rate
        );
    }
}