package com.qrtesy.modules.auth.features.logout;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class LogoutHandler {

    private final String cookieName;
    private final boolean cookieSecure;

    public LogoutHandler(
            @Value("${app.jwt.cookie-name:qrtesy_staff_token}") String cookieName,
            @Value("${app.jwt.cookie-secure:false}") boolean cookieSecure
    ) {
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
    }

    public ResponseCookie handle() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "None" : "Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}