package com.qrtesy.modules.registrations.features.staffattendees;

import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import com.qrtesy.shared.exception.DomainException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExportEventAttendeesCsvHandler {

    private static final DateTimeFormatter PHT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'PHT'");

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final ZoneId manilaZoneId;

    public ExportEventAttendeesCsvHandler(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            ZoneId manilaZoneId
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.manilaZoneId = manilaZoneId;
    }

    public record CsvExportResult(String filename, byte[] content) {
    }

    @Transactional(readOnly = true)
    public CsvExportResult handle(UUID eventId, String rawQuery) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "Event was not found."
                ));

        String searchPattern = (rawQuery == null || rawQuery.isBlank())
                ? "%"
                : "%" + rawQuery.trim().toLowerCase(Locale.ROOT) + "%";

        List<RegistrationEntity> rows = registrationRepository.searchByEventId(eventId, searchPattern);

        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF'); // UTF-8 BOM for Excel compatibility
        csv.append("Ticket Code,Full Name,Email,Mobile,Status,Waitlist Position,Registered At (PHT),Checked In At (PHT)\r\n");

        for (RegistrationEntity r : rows) {
            String waitlistPos = r.getStatus() == RegistrationStatus.WAITLISTED
                    ? String.valueOf(registrationRepository.findWaitlistPosition(
                    r.getEventId(), r.getRegisteredAt(), r.getId()
            ))
                    : "";

            String registeredAtStr = r.getRegisteredAt().atZone(manilaZoneId).format(PHT_FORMATTER);
            String checkedInAtStr = r.getCheckedInAt() != null
                    ? r.getCheckedInAt().atZone(manilaZoneId).format(PHT_FORMATTER)
                    : "";

            csv.append(escapeCsv(r.getTicketCode())).append(',')
                    .append(escapeCsv(r.getFullName())).append(',')
                    .append(escapeCsv(r.getEmail())).append(',')
                    .append(escapeCsv(r.getMobile())).append(',')
                    .append(escapeCsv(r.getStatus().name())).append(',')
                    .append(escapeCsv(waitlistPos)).append(',')
                    .append(escapeCsv(registeredAtStr)).append(',')
                    .append(escapeCsv(checkedInAtStr)).append("\r\n");
        }

        String slug = event.getTitle()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            slug = "event";
        }

        return new CsvExportResult(
                slug + "-attendees.csv",
                csv.toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String escapeCsv(String raw) {
        if (raw == null) {
            return "";
        }
        String sanitized = raw;
        if (!sanitized.isEmpty()) {
            char first = sanitized.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@') {
                sanitized = "'" + sanitized;
            }
        }
        boolean needsQuotes = sanitized.contains(",")
                || sanitized.contains("\"")
                || sanitized.contains("\n")
                || sanitized.contains("\r");
        if (!needsQuotes) {
            return sanitized;
        }
        return "\"" + sanitized.replace("\"", "\"\"") + "\"";
    }
}