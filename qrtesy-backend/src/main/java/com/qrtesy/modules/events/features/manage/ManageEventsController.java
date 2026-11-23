package com.qrtesy.modules.events.features.manage;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/events")
public class ManageEventsController {

    private final ManageEventsHandler handler;

    public ManageEventsController(ManageEventsHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public List<StaffEventResponse> listAllEvents() {
        return handler.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffEventResponse createEvent(@Valid @RequestBody SaveEventRequest request) {
        return handler.create(request);
    }

    @PutMapping("/{eventId}")
    public StaffEventResponse updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody SaveEventRequest request
    ) {
        return handler.update(eventId, request);
    }

    @PatchMapping("/{eventId}/close")
    public StaffEventResponse closeEvent(@PathVariable UUID eventId) {
        return handler.close(eventId);
    }
}