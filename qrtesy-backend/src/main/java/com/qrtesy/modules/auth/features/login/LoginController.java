package com.qrtesy.modules.auth.features.login;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginHandler handler;

    public LoginController(LoginHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/login")
    public ResponseEntity<StaffUserResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginHandler.LoginResult result = handler.handle(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.authCookie().toString())
                .body(result.user());
    }
}