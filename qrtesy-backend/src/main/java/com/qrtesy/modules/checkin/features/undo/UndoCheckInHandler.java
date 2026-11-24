package com.qrtesy.modules.checkin.features.undo;

import com.qrtesy.modules.audit.domain.AuditAction;
import com.qrtesy.modules.audit.domain.AuditLogService;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import com.qrtesy.shared.exception.DomainException;
import com.qrtesy.shared.security.JwtCookieFilter.StaffPrincipal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UndoCheckInHandler {

    private final RegistrationRepository registrationRepository;
    private final AuditLogService auditLogService;

    public UndoCheckInHandler(
            RegistrationRepository registrationRepository,
            AuditLogService auditLogService
    ) {
        this.registrationRepository = registrationRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public UndoCheckInResponse handle(UUID registrationId, StaffPrincipal staff) {
        if (staff == null) {
            throw new DomainException(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Authentication required."
            );
        }

        RegistrationEntity registration = registrationRepository.findByIdForUpdate(registrationId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "REGISTRATION_NOT_FOUND",
                        "Registration was not found."
                ));

        if (registration.getStatus() != RegistrationStatus.CHECKED_IN) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "NOT_CHECKED_IN",
                    "Only checked-in registrations can have their check-in undone."
            );
        }

        registration.undoCheckIn();
        RegistrationEntity saved = registrationRepository.save(registration);

        auditLogService.logStaffAction(
                staff,
                saved.getEventId(),
                saved.getId(),
                AuditAction.CHECKIN_UNDONE,
                "Reverted check-in for " + saved.getFullName() + " (" + saved.getTicketCode() + ")."
        );

        return new UndoCheckInResponse(
                saved.getId(),
                saved.getEventId(),
                saved.getTicketCode(),
                saved.getFullName(),
                saved.getStatus().name(),
                "Check-in reverted to Confirmed."
        );
    }
}