package com.qrtesy.modules.registrations.features.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RegisterAttendeeRequest(
        @NotNull(message = "Event ID is required.")
        UUID eventId,

        @NotBlank(message = "Full name is required.")
        @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters.")
        String fullName,

        @NotBlank(message = "Email address is required.")
        @Email(message = "Please provide a valid email address.")
        @Size(max = 160, message = "Email must not exceed 160 characters.")
        String email,

        @NotBlank(message = "Mobile number is required.")
        @Pattern(regexp = "^09\\d{9}$", message = "Mobile number must be a valid 11-digit Philippine number starting with 09.")
        String mobile
) {
}