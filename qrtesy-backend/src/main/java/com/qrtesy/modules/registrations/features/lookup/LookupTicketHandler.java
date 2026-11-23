package com.qrtesy.modules.registrations.features.lookup;

import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.TicketDetailsProjection;
import com.qrtesy.modules.registrations.features.register.RegistrationResponse;
import com.qrtesy.shared.exception.DomainException;
import java.time.ZoneId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LookupTicketHandler {

    private final RegistrationRepository registrationRepository;
    private final ZoneId manilaZoneId;

    public LookupTicketHandler(RegistrationRepository registrationRepository, ZoneId manilaZoneId) {
        this.registrationRepository = registrationRepository;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional(readOnly = true)
    public RegistrationResponse handle(String rawTicketCode) {
        String normalizedCode = rawTicketCode == null ? "" : rawTicketCode.trim();
        if (normalizedCode.isEmpty()) {
            throw new DomainException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_TICKET_CODE",
                    "Ticket code must not be empty."
            );
        }

        TicketDetailsProjection ticket = registrationRepository.findTicketDetailsByCode(normalizedCode)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "TICKET_NOT_FOUND",
                        "No registration was found for that ticket code."
                ));

        return new RegistrationResponse(
                ticket.getRegistrationId(),
                ticket.getTicketCode(),
                ticket.getFullName(),
                ticket.getEmail(),
                ticket.getMobile(),
                ticket.getStatus(),
                ticket.getRegisteredAt().atZone(manilaZoneId).toOffsetDateTime(),
                ticket.getCheckedInAt() != null
                        ? ticket.getCheckedInAt().atZone(manilaZoneId).toOffsetDateTime()
                        : null,
                ticket.getEventId(),
                ticket.getEventTitle(),
                ticket.getEventVenue(),
                ticket.getEventStartsAt().atZone(manilaZoneId).toOffsetDateTime(),
                ticket.getEventEndsAt().atZone(manilaZoneId).toOffsetDateTime(),
                manilaZoneId.getId()
        );
    }
}