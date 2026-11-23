package com.qrtesy.modules.events.features.getdetails;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class GetEventDetailsController {

    private final GetEventDetailsHandler handler;

    public GetEventDetailsController(GetEventDetailsHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/{eventId}")
    public EventDetailsResponse getEventDetails(@PathVariable UUID eventId) {
        return handler.handle(eventId);
    }
}