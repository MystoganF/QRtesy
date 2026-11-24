package com.qrtesy.modules.auth.infrastructure;

import com.qrtesy.modules.auth.domain.StaffRole;
import com.qrtesy.modules.auth.domain.StaffUserEntity;
import com.qrtesy.modules.auth.domain.StaffUserRepository;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StaffAccountSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StaffAccountSeeder.class);

    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String organizerName;
    private final String organizerUsername;
    private final String organizerPassword;
    private final String doorName;
    private final String doorUsername;
    private final String doorPassword;

    public StaffAccountSeeder(
            StaffUserRepository staffUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.staff-name:Event Organizer}") String organizerName,
            @Value("${app.seed.staff-username:staff}") String organizerUsername,
            @Value("${app.seed.staff-password:ChangeMe123!}") String organizerPassword,
            @Value("${app.seed.door-name:Door Check-in Staff}") String doorName,
            @Value("${app.seed.door-username:door}") String doorUsername,
            @Value("${app.seed.door-password:DoorStaff123!}") String doorPassword
    ) {
        this.staffUserRepository = staffUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizerName = organizerName;
        this.organizerUsername = organizerUsername;
        this.organizerPassword = organizerPassword;
        this.doorName = doorName;
        this.doorUsername = doorUsername;
        this.doorPassword = doorPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAccountIfMissing(organizerName, organizerUsername, organizerPassword, StaffRole.ORGANIZER);
        seedAccountIfMissing(doorName, doorUsername, doorPassword, StaffRole.DOOR_STAFF);
    }

    private void seedAccountIfMissing(String name, String rawUsername, String rawPassword, StaffRole role) {
        String normalizedUsername = rawUsername.trim().toLowerCase(Locale.ROOT);
        if (normalizedUsername.isEmpty() || staffUserRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            return;
        }

        StaffUserEntity account = StaffUserEntity.create(
                name.trim(),
                normalizedUsername,
                passwordEncoder.encode(rawPassword),
                role,
                Instant.now()
        );
        staffUserRepository.save(account);
        log.info("Seeded default {} account '{}'", role.name(), normalizedUsername);
    }
}