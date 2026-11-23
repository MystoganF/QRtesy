package com.qrtesy.modules.registrations.features.cancel;

import com.qrtesy.modules.registrations.features.register.RegistrationResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CancelRegistrationController {

    private final CancelRegistrationHandler handler;

    public CancelRegistrationController(CancelRegistrationHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/api/registrations/{ticketCode}/cancel")
    public RegistrationResponse cancelByAttendee(
            @PathVariable String ticketCode,
            @Valid @RequestBody CancelRegistrationRequest request
    ) {
        return handler.cancelByAttendee(ticketCode, request.email());
    }

    @PostMapping("/api/staff/registrations/{registrationId}/cancel")
    public RegistrationResponse cancelByStaff(@PathVariable UUID registrationId) {
        return handler.cancelByStaff(registrationId);
    }
}