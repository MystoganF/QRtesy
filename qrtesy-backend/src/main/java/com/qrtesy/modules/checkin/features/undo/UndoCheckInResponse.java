package com.qrtesy.modules.checkin.features.undo;

import java.util.UUID;

public record UndoCheckInResponse(
        UUID registrationId,
        UUID eventId,
        String ticketCode,
        String fullName,
        String status,
        String message
) {
}