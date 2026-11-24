package com.qrtesy.modules.registrations.features.lookup;

import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.TicketDetailsProjection;
import com.qrtesy.modules.registrations.features.register.RegistrationResponse;
import com.qrtesy.shared.exception.DomainException;
import com.qrtesy.shared.security.TicketQrSigner;
import java.time.ZoneId;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LookupTicketHandler {

    private final RegistrationRepository registrationRepository;
    private final TicketQrSigner ticketQrSigner;
    private final ZoneId manilaZoneId;

    public LookupTicketHandler(
            RegistrationRepository registrationRepository,
            TicketQrSigner ticketQrSigner,
            ZoneId manilaZoneId
    ) {
        this.registrationRepository = registrationRepository;
        this.ticketQrSigner = ticketQrSigner;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional(readOnly = true)
    public RegistrationResponse handle(String rawTicketCode, String rawEmail) {
        String normalizedCode = rawTicketCode == null ? "" : rawTicketCode.trim();
        String normalizedEmail = rawEmail == null ? "" : rawEmail.trim().toLowerCase(Locale.ROOT);

        if (normalizedCode.isEmpty() || normalizedEmail.isEmpty()) {
            throw new DomainException(
                    HttpStatus.BAD_REQUEST,
                    "MISSING_LOOKUP_FIELDS",
                    "Both ticket code and email address are required."
            );
        }

        TicketDetailsProjection ticket = registrationRepository.findTicketDetailsByCode(normalizedCode)
                .filter(t -> t.getEmail().trim().toLowerCase(Locale.ROOT).equals(normalizedEmail))
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "TICKET_NOT_FOUND",
                        "Ticket not found."
                ));

        String qrToken = ticketQrSigner.sign(ticket.getEventId(), ticket.getTicketCode());

        return new RegistrationResponse(
                ticket.getRegistrationId(),
                ticket.getTicketCode(),
                qrToken,
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
                manilaZoneId.getId(),
                null
        );
    }
}