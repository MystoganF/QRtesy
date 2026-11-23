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
    private final String seedName;
    private final String seedUsername;
    private final String seedPassword;

    public StaffAccountSeeder(
            StaffUserRepository staffUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.staff-name:Event Organizer}") String seedName,
            @Value("${app.seed.staff-username:staff}") String seedUsername,
            @Value("${app.seed.staff-password:ChangeMe123!}") String seedPassword
    ) {
        this.staffUserRepository = staffUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedName = seedName;
        this.seedUsername = seedUsername;
        this.seedPassword = seedPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalizedUsername = seedUsername.trim().toLowerCase(Locale.ROOT);
        if (normalizedUsername.isEmpty() || staffUserRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            return;
        }

        StaffUserEntity organizer = StaffUserEntity.create(
                seedName.trim(),
                normalizedUsername,
                passwordEncoder.encode(seedPassword),
                StaffRole.ORGANIZER,
                Instant.now()
        );
        staffUserRepository.save(organizer);
        log.info("Seeded default staff account '{}'", normalizedUsername);
    }
}