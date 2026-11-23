package com.qrtesy.modules.registrations.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WaitlistPromotionService {

    private final RegistrationRepository registrationRepository;

    public WaitlistPromotionService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public int promoteFromWaitlist(UUID eventId, int capacity) {
        long occupied = registrationRepository.countOccupiedSpots(eventId);
        int openSpots = Math.max(0, capacity - (int) occupied);
        if (openSpots == 0) {
            return 0;
        }

        List<RegistrationEntity> waitlist = registrationRepository
                .findWaitlistedByEventIdOrderByFifoForUpdate(eventId);

        int toPromote = Math.min(openSpots, waitlist.size());
        for (int i = 0; i < toPromote; i++) {
            waitlist.get(i).promoteToConfirmed();
        }

        if (toPromote > 0) {
            registrationRepository.saveAll(waitlist.subList(0, toPromote));
        }

        return toPromote;
    }
}