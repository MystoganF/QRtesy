package com.qrtesy.modules.audit.features.list;

import com.qrtesy.modules.audit.domain.AuditLogEntity;
import com.qrtesy.modules.audit.domain.AuditLogRepository;
import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListAuditLogsHandler {

    private final AuditLogRepository auditLogRepository;
    private final EventRepository eventRepository;
    private final ZoneId manilaZoneId;

    public ListAuditLogsHandler(
            AuditLogRepository auditLogRepository,
            EventRepository eventRepository,
            ZoneId manilaZoneId
    ) {
        this.auditLogRepository = auditLogRepository;
        this.eventRepository = eventRepository;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional(readOnly = true)
    public List<AuditLogItemResponse> handle(UUID eventId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<AuditLogEntity> logs = auditLogRepository.findRecent(eventId, PageRequest.of(0, safeLimit));

        Set<UUID> eventIds = logs.stream()
                .map(AuditLogEntity::getEventId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, String> eventTitles = eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(EventEntity::getId, EventEntity::getTitle));

        return logs.stream()
                .map(log -> new AuditLogItemResponse(
                        log.getId(),
                        log.getEventId(),
                        log.getEventId() != null ? eventTitles.get(log.getEventId()) : null,
                        log.getRegistrationId(),
                        log.getActorType().name(),
                        log.getActorLabel(),
                        log.getAction().name(),
                        log.getSummary(),
                        log.getCreatedAt().atZone(manilaZoneId).toOffsetDateTime(),
                        manilaZoneId.getId()
                ))
                .toList();
    }
}