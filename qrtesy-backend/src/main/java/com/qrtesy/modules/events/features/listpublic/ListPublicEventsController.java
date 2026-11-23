package com.qrtesy.modules.events.features.listpublic;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class ListPublicEventsController {

    private final ListPublicEventsHandler handler;

    public ListPublicEventsController(ListPublicEventsHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public List<PublicEventSummaryResponse> listPublicEvents() {
        return handler.handle();
    }
}