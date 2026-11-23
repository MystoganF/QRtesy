package com.qrtesy.modules.registrations.features.register;

import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.events.domain.EventStatus;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import com.qrtesy.shared.exception.DomainException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterAttendeeHandler {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final ZoneId manilaZoneId;

    public RegisterAttendeeHandler(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            ZoneId manilaZoneId
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.manilaZoneId = manilaZoneId;
    }

    @Transactional
    public RegistrationResponse handle(RegisterAttendeeRequest request) {
        EventEntity event = eventRepository.findByIdForUpdate(request.eventId())
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found or is not open for registration."
                ));

        Instant now = Instant.now();
        if (event.getEndsAt().isBefore(now)) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "EVENT_ENDED",
                    "Registration is closed because this event has already ended."
            );
        }

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        String cleanName = request.fullName().trim();
        String cleanMobile = request.mobile().trim();

        if (registrationRepository.existsActiveRegistration(event.getId(), normalizedEmail)) {
            throw new DomainException(
                    HttpStatus.CONFLICT,
                    "ALREADY_REGISTERED",
                    "This email address is already registered or waitlisted for the selected event."
            );
        }

        long occupiedSpots = registrationRepository.countOccupiedSpots(event.getId());
        boolean joinWaitlist = occupiedSpots >= event.getCapacity();

        String ticketCode = generateUniqueTicketCode();
        RegistrationEntity toSave = joinWaitlist
                ? RegistrationEntity.createWaitlisted(event.getId(), cleanName, normalizedEmail, cleanMobile, ticketCode, now)
                : RegistrationEntity.createConfirmed(event.getId(), cleanName, normalizedEmail, cleanMobile, ticketCode, now);

        RegistrationEntity saved = registrationRepository.save(toSave);

        Integer waitlistPosition = saved.getStatus() == RegistrationStatus.WAITLISTED
                ? registrationRepository.findWaitlistPosition(event.getId(), saved.getRegisteredAt(), saved.getId())
                : null;

        return new RegistrationResponse(
                saved.getId(),
                saved.getTicketCode(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getMobile(),
                saved.getStatus().name(),
                saved.getRegisteredAt().atZone(manilaZoneId).toOffsetDateTime(),
                null,
                event.getId(),
                event.getTitle(),
                event.getVenue(),
                event.getStartsAt().atZone(manilaZoneId).toOffsetDateTime(),
                event.getEndsAt().atZone(manilaZoneId).toOffsetDateTime(),
                manilaZoneId.getId(),
                waitlistPosition
        );
    }

    private String generateUniqueTicketCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder("QRT-");
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            String candidate = sb.toString();
            if (!registrationRepository.existsByTicketCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique ticket code.");
    }
}