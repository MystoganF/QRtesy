package com.qrtesy.modules.auth.features.login;

import com.qrtesy.modules.auth.domain.StaffUserEntity;
import com.qrtesy.modules.auth.domain.StaffUserRepository;
import com.qrtesy.shared.exception.DomainException;
import com.qrtesy.shared.security.JwtTokenService;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginHandler {

    public record LoginResult(StaffUserResponse user, ResponseCookie authCookie) {}

    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final String cookieName;
    private final boolean cookieSecure;
    private final long expirationHours;

    public LoginHandler(
            StaffUserRepository staffUserRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            @Value("${app.jwt.cookie-name:qrtesy_staff_token}") String cookieName,
            @Value("${app.jwt.cookie-secure:false}") boolean cookieSecure,
            @Value("${app.jwt.expiration-hours:12}") long expirationHours
    ) {
        this.staffUserRepository = staffUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
        this.expirationHours = expirationHours;
    }

    @Transactional(readOnly = true)
    public LoginResult handle(LoginRequest request) {
        String username = request.username().trim();
        StaffUserEntity staff = staffUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS",
                        "Invalid username or password."
                ));

        if (!passwordEncoder.matches(request.password(), staff.getPasswordHash())) {
            throw new DomainException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS",
                    "Invalid username or password."
            );
        }

        String token = jwtTokenService.generateToken(
                staff.getId(),
                staff.getUsername(),
                staff.getRole().name()
        );

        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofHours(expirationHours))
                .build();

        StaffUserResponse user = new StaffUserResponse(
                staff.getId(),
                staff.getName(),
                staff.getUsername(),
                staff.getRole().name()
        );

        return new LoginResult(user, cookie);
    }
}