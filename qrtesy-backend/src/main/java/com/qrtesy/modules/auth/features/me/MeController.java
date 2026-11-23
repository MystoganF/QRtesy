package com.qrtesy.modules.auth.features.me;

import com.qrtesy.modules.auth.features.login.StaffUserResponse;
import com.qrtesy.shared.security.JwtCookieFilter.StaffPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class MeController {

    private final MeHandler handler;

    public MeController(MeHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/me")
    public StaffUserResponse me(@AuthenticationPrincipal StaffPrincipal principal) {
        return handler.handle(principal);
    }
}