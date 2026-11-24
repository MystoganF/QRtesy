package com.qrtesy.modules.registrations.domain;

import com.qrtesy.modules.audit.domain.AuditAction;
import com.qrtesy.modules.audit.domain.AuditLogService;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.notifications.domain.EmailNotificationService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WaitlistPromotionService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final AuditLogService auditLogService;
    private final EmailNotificationService emailNotificationService;

    public WaitlistPromotionService(
            RegistrationRepository registrationRepository,
            EventRepository eventRepository,
            AuditLogService auditLogService,
            EmailNotificationService emailNotificationService
    ) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.auditLogService = auditLogService;
        this.emailNotificationService = emailNotificationService;
    }

    @Transactional
    public int promoteFromWaitlist(UUID eventId, int capacity) {
        long occupied = registrationRepository.countOccupiedSpots(eventId);
        int openSpots = Math.max(0, capacity - (int) occupied);
        if (openSpots == 0) {
            return 0;
        }

        List<RegistrationEntity> waitlist = registrationRepository
                .findWaitlistedByEventIdOrderByFifoForUpdate(eventId);

        int toPromote = Math.min(openSpots, waitlist.size());
        List<RegistrationEntity> promoted = waitlist.subList(0, toPromote);

        for (RegistrationEntity reg : promoted) {
            reg.promoteToConfirmed();
        }

        if (!promoted.isEmpty()) {
            registrationRepository.saveAll(promoted);
            eventRepository.findById(eventId).ifPresent(event -> {
                for (RegistrationEntity reg : promoted) {
                    auditLogService.logSystemAction(
                            eventId,
                            reg.getId(),
                            AuditAction.WAITLIST_PROMOTED,
                            "Promoted " + reg.getFullName() + " (" + reg.getTicketCode() + ") from waitlist to Confirmed."
                    );
                    emailNotificationService.sendWaitlistPromotionAfterCommit(event, reg);
                }
            });
        }

        return toPromote;
    }
}