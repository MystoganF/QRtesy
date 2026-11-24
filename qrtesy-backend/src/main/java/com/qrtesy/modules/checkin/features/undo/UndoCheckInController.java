package com.qrtesy.modules.checkin.features.undo;

import com.qrtesy.shared.security.JwtCookieFilter.StaffPrincipal;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/registrations")
public class UndoCheckInController {

    private final UndoCheckInHandler handler;

    public UndoCheckInController(UndoCheckInHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/{registrationId}/undo-check-in")
    public UndoCheckInResponse undoCheckIn(
            @PathVariable UUID registrationId,
            @AuthenticationPrincipal StaffPrincipal staff
    ) {
        return handler.handle(registrationId, staff);
    }
}