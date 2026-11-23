package com.qrtesy.modules.registrations.domain;

import java.time.Instant;
import java.util.UUID;

public interface TicketDetailsProjection {
    UUID getRegistrationId();
    String getTicketCode();
    String getFullName();
    String getEmail();
    String getMobile();
    String getStatus();
    Instant getRegisteredAt();
    Instant getCheckedInAt();
    UUID getEventId();
    String getEventTitle();
    String getEventVenue();
    Instant getEventStartsAt();
    Instant getEventEndsAt();
    Integer getWaitlistPosition();
}