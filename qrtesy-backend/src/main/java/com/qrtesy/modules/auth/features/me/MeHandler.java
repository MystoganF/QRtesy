package com.qrtesy.modules.auth.features.me;

import com.qrtesy.modules.auth.domain.StaffUserEntity;
import com.qrtesy.modules.auth.domain.StaffUserRepository;
import com.qrtesy.modules.auth.features.login.StaffUserResponse;
import com.qrtesy.shared.exception.DomainException;
import com.qrtesy.shared.security.JwtCookieFilter.StaffPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeHandler {

    private final StaffUserRepository staffUserRepository;

    public MeHandler(StaffUserRepository staffUserRepository) {
        this.staffUserRepository = staffUserRepository;
    }

    @Transactional(readOnly = true)
    public StaffUserResponse handle(StaffPrincipal principal) {
        if (principal == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required.");
        }

        StaffUserEntity staff = staffUserRepository.findById(principal.id())
                .orElseThrow(() -> new DomainException(
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHORIZED",
                        "Staff account no longer exists."
                ));

        return new StaffUserResponse(
                staff.getId(),
                staff.getName(),
                staff.getUsername(),
                staff.getRole().name()
        );
    }
}