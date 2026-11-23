package com.qrtesy.modules.auth.features.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username is required.")
        @Size(max = 60, message = "Username must not exceed 60 characters.")
        String username,

        @NotBlank(message = "Password is required.")
        @Size(max = 128, message = "Password must not exceed 128 characters.")
        String password
) {
}