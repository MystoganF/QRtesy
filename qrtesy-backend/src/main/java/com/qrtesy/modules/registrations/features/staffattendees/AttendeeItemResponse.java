package com.qrtesy.modules.registrations.features.staffattendees;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AttendeeItemResponse(
        UUID registrationId,
        String ticketCode,
        String fullName,
        String email,
        String mobile,
        String status,
        OffsetDateTime registeredAt,
        OffsetDateTime checkedInAt,
        Integer waitlistPosition
) {
}