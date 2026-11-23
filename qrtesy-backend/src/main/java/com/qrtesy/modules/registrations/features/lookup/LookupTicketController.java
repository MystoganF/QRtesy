package com.qrtesy.modules.registrations.features.lookup;

import com.qrtesy.modules.registrations.features.register.RegistrationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registrations")
public class LookupTicketController {

    private final LookupTicketHandler handler;

    public LookupTicketController(LookupTicketHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/{ticketCode}")
    public RegistrationResponse lookupTicket(
            @PathVariable String ticketCode,
            @RequestParam(name = "email") String email
    ) {
        return handler.handle(ticketCode, email);
    }
}