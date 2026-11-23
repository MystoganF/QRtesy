package com.qrtesy.modules.checkin.features.scan;

import com.qrtesy.shared.security.JwtCookieFilter.StaffPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/events")
public class ScanTicketController {

    private final ScanTicketHandler handler;

    public ScanTicketController(ScanTicketHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/{eventId}/check-in")
    public ScanTicketResponse scanTicket(
            @PathVariable UUID eventId,
            @Valid @RequestBody ScanTicketRequest request,
            @AuthenticationPrincipal StaffPrincipal staff
    ) {
        return handler.handle(eventId, request, staff);
    }
}