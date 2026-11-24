package com.qrtesy.modules.checkin.features.scan;

import com.qrtesy.modules.audit.domain.AuditAction;
import com.qrtesy.modules.audit.domain.AuditLogService;
import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.events.domain.EventStatus;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import com.qrtesy.shared.exception.DomainException;
import com.qrtesy.shared.security.JwtCookieFilter.StaffPrincipal;
import com.qrtesy.shared.security.TicketQrSigner;
import com.qrtesy.shared.security.TicketQrSigner.VerificationResult;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScanTicketHandler {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final TicketQrSigner ticketQrSigner;
    private final ZoneId manilaZoneId;
    private final AuditLogService auditLogService;

    @Autowired
    public ScanTicketHandler(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            TicketQrSigner ticketQrSigner,
            ZoneId manilaZoneId,
            AuditLogService auditLogService
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.ticketQrSigner = ticketQrSigner;
        this.manilaZoneId = manilaZoneId;
        this.auditLogService = auditLogService;
    }

    public ScanTicketHandler(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            TicketQrSigner ticketQrSigner,
            ZoneId manilaZoneId
    ) {
        this(eventRepository, registrationRepository, ticketQrSigner, manilaZoneId, null);
    }

    @Transactional
    public ScanTicketResponse handle(UUID eventId, ScanTicketRequest request, StaffPrincipal staff) {
        if (staff == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required.");
        }

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found."
                ));

        if (event.getStatus() == EventStatus.DRAFT) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "EVENT_NOT_PUBLISHED",
                    "Check-in is not allowed while this event is still in Draft status."
            );
        }

        Instant now = Instant.now();
        LocalDate todayManila = now.atZone(manilaZoneId).toLocalDate();
        LocalDate eventStartDay = event.getStartsAt().atZone(manilaZoneId).toLocalDate();
        LocalDate eventEndDay = event.getEndsAt().atZone(manilaZoneId).toLocalDate();

        if (todayManila.isBefore(eventStartDay) || todayManila.isAfter(eventEndDay)) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "NOT_EVENT_DAY",
                    "Check-in is only allowed on the event day."
            );
        }

        VerificationResult verification = ticketQrSigner.verifyAndExtract(eventId, request.ticketCode());
        if (!verification.valid()) {
            return new ScanTicketResponse(
                    CheckInOutcome.INVALID_TICKET,
                    verification.errorMessage(),
                    null,
                    request.ticketCode().trim(),
                    null,
                    null,
                    null,
                    manilaZoneId.getId()
            );
        }

        String normalizedCode = verification.ticketCode();
        Optional<RegistrationEntity> match = registrationRepository
                .findByEventIdAndTicketCodeForUpdate(eventId, normalizedCode);

        if (match.isEmpty()) {
            return new ScanTicketResponse(
                    CheckInOutcome.INVALID_TICKET,
                    "Invalid ticket — code not found for this event.",
                    null,
                    normalizedCode,
                    null,
                    null,
                    null,
                    manilaZoneId.getId()
            );
        }

        RegistrationEntity reg = match.get();

        if (reg.getStatus() == RegistrationStatus.CONFIRMED) {
            reg.markCheckedIn(now, staff.id());
            registrationRepository.save(reg);
            OffsetDateTime checkedInTime = now.atZone(manilaZoneId).toOffsetDateTime();

            if (auditLogService != null) {
                auditLogService.logStaffAction(
                        staff,
                        eventId,
                        reg.getId(),
                        AuditAction.TICKET_CHECKED_IN,
                        "Checked in " + reg.getFullName() + " (" + reg.getTicketCode() + ")."
                );
            }

            return new ScanTicketResponse(
                    CheckInOutcome.CHECKED_IN,
                    "Checked in",
                    reg.getId(),
                    reg.getTicketCode(),
                    reg.getFullName(),
                    reg.getStatus().name(),
                    checkedInTime,
                    manilaZoneId.getId()
            );
        }

        if (reg.getStatus() == RegistrationStatus.CHECKED_IN) {
            OffsetDateTime firstTime = reg.getCheckedInAt() != null
                    ? reg.getCheckedInAt().atZone(manilaZoneId).toOffsetDateTime()
                    : null;

            return new ScanTicketResponse(
                    CheckInOutcome.ALREADY_CHECKED_IN,
                    "Already checked in",
                    reg.getId(),
                    reg.getTicketCode(),
                    reg.getFullName(),
                    reg.getStatus().name(),
                    firstTime,
                    manilaZoneId.getId()
            );
        }

        String statusMessage = reg.getStatus() == RegistrationStatus.WAITLISTED
                ? "On waitlist — not confirmed"
                : "Cancelled";

        return new ScanTicketResponse(
                CheckInOutcome.NOT_CONFIRMED,
                statusMessage,
                reg.getId(),
                reg.getTicketCode(),
                reg.getFullName(),
                reg.getStatus().name(),
                null,
                manilaZoneId.getId()
        );
    }
}