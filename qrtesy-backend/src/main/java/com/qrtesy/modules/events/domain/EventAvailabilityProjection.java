package com.qrtesy.modules.events.domain;

import java.time.Instant;
import java.util.UUID;

public interface EventAvailabilityProjection {
    UUID getId();
    String getTitle();
    String getDescription();
    String getVenue();
    Instant getStartsAt();
    Instant getEndsAt();
    Integer getCapacity();
    String getStatus();
    Long getOccupiedCount();
}