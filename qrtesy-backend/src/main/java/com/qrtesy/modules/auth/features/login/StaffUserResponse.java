package com.qrtesy.modules.auth.features.login;

import java.util.UUID;

public record StaffUserResponse(
        UUID id,
        String name,
        String username,
        String role
) {
}