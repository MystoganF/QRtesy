package com.qrtesy.modules.checkin.features.scan;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScanTicketResponse(
        CheckInOutcome outcome,
        String message,
        UUID registrationId,
        String ticketCode,
        String fullName,
        String status,
        OffsetDateTime checkedInAt,
        String timezone
) {
}