package com.qrtesy.modules.registrations.features.cancel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CancelRegistrationRequest(
        @NotBlank(message = "Email address is required to cancel a registration.")
        @Email(message = "Please enter a valid email address.")
        String email
) {
}