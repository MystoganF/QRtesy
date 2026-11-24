package com.qrtesy.modules.registrations.features.promote;

import com.qrtesy.modules.registrations.features.register.RegistrationResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/registrations")
public class PromoteWaitlistController {

    private final PromoteWaitlistHandler handler;

    public PromoteWaitlistController(PromoteWaitlistHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/{registrationId}/promote")
    public RegistrationResponse promote(@PathVariable UUID registrationId) {
        return handler.promote(registrationId);
    }
}