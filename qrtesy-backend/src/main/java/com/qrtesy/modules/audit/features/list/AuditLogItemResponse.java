package com.qrtesy.modules.audit.features.list;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogItemResponse(
        UUID id,
        UUID eventId,
        String eventTitle,
        UUID registrationId,
        String actorType,
        String actorLabel,
        String action,
        String summary,
        OffsetDateTime createdAt,
        String timezone
) {
}