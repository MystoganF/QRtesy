package com.qrtesy.modules.registrations.features.staffattendees;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/events")
public class ListEventAttendeesController {

    private final ListEventAttendeesHandler handler;

    public ListEventAttendeesController(ListEventAttendeesHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/{eventId}/attendees")
    public EventAttendeesResponse listAttendees(
            @PathVariable UUID eventId,
            @RequestParam(name = "q", required = false) String query
    ) {
        return handler.handle(eventId, query);
    }
}