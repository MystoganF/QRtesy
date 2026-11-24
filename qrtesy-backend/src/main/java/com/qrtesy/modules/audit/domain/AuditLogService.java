package com.qrtesy.modules.audit.domain;

import com.qrtesy.shared.security.JwtCookieFilter.StaffPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void logCurrentStaffAction(
            UUID eventId,
            UUID registrationId,
            AuditAction action,
            String summary
    ) {
        StaffPrincipal staff = resolveAuthenticatedStaff();
        if (staff != null) {
            logStaffAction(staff, eventId, registrationId, action, summary);
        } else {
            logSystemAction(eventId, registrationId, action, summary);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void logStaffAction(
            StaffPrincipal staff,
            UUID eventId,
            UUID registrationId,
            AuditAction action,
            String summary
    ) {
        UUID actorId = staff != null ? staff.id() : null;
        String actorLabel = staff != null ? staff.username() : "staff";
        auditLogRepository.save(
                AuditLogEntity.create(
                        eventId,
                        registrationId,
                        AuditActorType.STAFF,
                        actorId,
                        actorLabel,
                        action,
                        summary,
                        Instant.now()
                )
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void logAttendeeAction(
            String attendeeEmail,
            UUID eventId,
            UUID registrationId,
            AuditAction action,
            String summary
    ) {
        auditLogRepository.save(
                AuditLogEntity.create(
                        eventId,
                        registrationId,
                        AuditActorType.ATTENDEE,
                        null,
                        attendeeEmail,
                        action,
                        summary,
                        Instant.now()
                )
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void logSystemAction(
            UUID eventId,
            UUID registrationId,
            AuditAction action,
            String summary
    ) {
        auditLogRepository.save(
                AuditLogEntity.create(
                        eventId,
                        registrationId,
                        AuditActorType.SYSTEM,
                        null,
                        "SYSTEM",
                        action,
                        summary,
                        Instant.now()
                )
        );
    }

    private StaffPrincipal resolveAuthenticatedStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof StaffPrincipal staff) {
            return staff;
        }
        return null;
    }
}