package com.qrtesy.modules.events.domain;

import java.time.Instant;
import java.util.UUID;

public interface DashboardEventProjection {
    UUID getId();
    String getTitle();
    String getVenue();
    Instant getStartsAt();
    Instant getEndsAt();
    int getCapacity();
    String getStatus();
    long getConfirmedCount();
    long getCheckedInCount();
    long getWaitlistCount();
    long getCancelledCount();
}