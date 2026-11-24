package com.qrtesy.modules.registrations.features.staffattendees;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/events")
public class ListEventAttendeesController {

    private final ListEventAttendeesHandler handler;
    private final ExportEventAttendeesCsvHandler csvHandler;

    public ListEventAttendeesController(
            ListEventAttendeesHandler handler,
            ExportEventAttendeesCsvHandler csvHandler
    ) {
        this.handler = handler;
        this.csvHandler = csvHandler;
    }

    @GetMapping("/{eventId}/attendees")
    public EventAttendeesResponse listAttendees(
            @PathVariable UUID eventId,
            @RequestParam(name = "q", required = false) String query
    ) {
        return handler.handle(eventId, query);
    }

    @GetMapping("/{eventId}/attendees/export")
    public ResponseEntity<byte[]> exportAttendeesCsv(
            @PathVariable UUID eventId,
            @RequestParam(name = "q", required = false) String query
    ) {
        ExportEventAttendeesCsvHandler.CsvExportResult result = csvHandler.handle(eventId, query);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(result.filename()).build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.content());
    }
}