package com.qrtesy.modules.events.features.dashboard;

import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.events.domain.EventStatus;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffDashboardHandler {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final ZoneId manilaZoneId;

    public StaffDashboardHandler(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            ZoneId manilaZoneId
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional(readOnly = true)
    public StaffDashboardResponse handle() {
        List<EventEntity> allEvents = eventRepository.findAll().stream()
                .sorted(Comparator.comparing(EventEntity::getStartsAt).reversed())
                .toList();

        int publishedEvents = 0;
        int draftEvents = 0;
        int closedEvents = 0;
        int totalCapacity = 0;
        int totalOccupied = 0;
        int totalCheckedIn = 0;
        int totalWaitlisted = 0;

        List<DashboardEventSummary> summaries = allEvents.stream()
                .map(this::toSummary)
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
                totalWaitlisted,
                overallRate,
                summaries
        );
    }

    private DashboardEventSummary toSummary(EventEntity event) {
        int confirmed = (int) registrationRepository.countByEventIdAndStatus(
                event.getId(), RegistrationStatus.CONFIRMED
        );
        int checkedIn = (int) registrationRepository.countByEventIdAndStatus(
                event.getId(), RegistrationStatus.CHECKED_IN
        );
        int waitlisted = (int) registrationRepository.countByEventIdAndStatus(
                event.getId(), RegistrationStatus.WAITLISTED
        );
        int cancelled = (int) registrationRepository.countByEventIdAndStatus(
                event.getId(), RegistrationStatus.CANCELLED
        );

        int occupied = confirmed + checkedIn;
        int remaining = Math.max(0, event.getCapacity() - occupied);
        double rate = occupied > 0
                ? Math.round((checkedIn * 1000.0) / occupied) / 10.0
                : 0.0;

        return new DashboardEventSummary(
                event.getId(),
                event.getTitle(),
                event.getVenue(),
                event.getStartsAt().atZone(manilaZoneId).toOffsetDateTime(),
                event.getEndsAt().atZone(manilaZoneId).toOffsetDateTime(),
                manilaZoneId.getId(),
                event.getStatus().name(),
                event.getCapacity(),
                confirmed,
                checkedIn,
                occupied,
                remaining,
                waitlisted,
                cancelled,
                rate
        );
    }
}