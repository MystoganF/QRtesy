package com.qrtesy.modules.registrations.features.register;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegistrationResponse(
        UUID registrationId,
        String ticketCode,
        String qrToken,
        String fullName,
        String email,
        String mobile,
        String status,
        OffsetDateTime registeredAt,
        OffsetDateTime checkedInAt,
        UUID eventId,
        String eventTitle,
        String eventVenue,
        OffsetDateTime eventStartsAt,
        OffsetDateTime eventEndsAt,
        String timezone,
        Integer waitlistPosition
) {
    public RegistrationResponse(
            UUID registrationId,
            String ticketCode,
            String fullName,
            String email,
            String mobile,
            String status,
            OffsetDateTime registeredAt,
            OffsetDateTime checkedInAt,
            UUID eventId,
            String eventTitle,
            String eventVenue,
            OffsetDateTime eventStartsAt,
            OffsetDateTime eventEndsAt,
            String timezone,
            Integer waitlistPosition
    ) {
        this(
                registrationId,
                ticketCode,
                ticketCode,
                fullName,
                email,
                mobile,
                status,
                registeredAt,
                checkedInAt,
                eventId,
                eventTitle,
                eventVenue,
                eventStartsAt,
                eventEndsAt,
                timezone,
                waitlistPosition
        );
    }

    public RegistrationResponse(
            UUID registrationId,
            String ticketCode,
            String fullName,
            String email,
            String mobile,
            String status,
            OffsetDateTime registeredAt,
            OffsetDateTime checkedInAt,
            UUID eventId,
            String eventTitle,
            String eventVenue,
            OffsetDateTime eventStartsAt,
            OffsetDateTime eventEndsAt,
            String timezone
    ) {
        this(
                registrationId,
                ticketCode,
                ticketCode,
                fullName,
                email,
                mobile,
                status,
                registeredAt,
                checkedInAt,
                eventId,
                eventTitle,
                eventVenue,
                eventStartsAt,
                eventEndsAt,
                timezone,
                null
        );
    }
}