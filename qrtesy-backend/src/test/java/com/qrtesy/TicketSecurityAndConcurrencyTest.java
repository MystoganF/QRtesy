package com.qrtesy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.qrtesy.modules.checkin.features.scan.CheckInOutcome;
import com.qrtesy.modules.checkin.features.scan.ScanTicketHandler;
import com.qrtesy.modules.checkin.features.scan.ScanTicketRequest;
import com.qrtesy.modules.checkin.features.scan.ScanTicketResponse;
import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.events.domain.EventRepository;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationRepository;
import com.qrtesy.shared.security.JwtCookieFilter.StaffPrincipal;
import com.qrtesy.shared.security.TicketQrSigner;
import com.qrtesy.shared.security.TicketQrSigner.VerificationResult;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class TicketSecurityAndConcurrencyTest {

    private static final String TEST_SECRET = "test-secret-key-with-at-least-32-characters-1234";
    private final TicketQrSigner signer = new TicketQrSigner(TEST_SECRET);

    @Test
    void signedQrTokenVerifiesForMatchingEventAndRejectsTampering() {
        UUID eventA = UUID.randomUUID();
        UUID eventB = UUID.randomUUID();
        String signedToken = signer.sign(eventA, "QRT-7K3P9Q");

        VerificationResult valid = signer.verifyAndExtract(eventA, signedToken);
        assertTrue(valid.valid());
        assertEquals("QRT-7K3P9Q", valid.ticketCode());

        VerificationResult wrongEvent = signer.verifyAndExtract(eventB, signedToken);
        assertFalse(wrongEvent.valid());

        VerificationResult forged = signer.verifyAndExtract(eventA, "QRT1.CDM-000001.TAMPERED0000");
        assertFalse(forged.valid());
    }

    @Test
    void simultaneousCheckInAllowsOnlyOneSuccessUnderLock() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();
        StaffPrincipal staff = new StaffPrincipal(staffId, "staff", "ORGANIZER");

        EventEntity event = mock(EventEntity.class);
        when(event.getStartsAt()).thenReturn(Instant.now().minus(1, ChronoUnit.HOURS));
        when(event.getEndsAt()).thenReturn(Instant.now().plus(3, ChronoUnit.HOURS));

        RegistrationEntity sharedRegistration = RegistrationEntity.createConfirmed(
                eventId,
                "Maria Santos",
                "maria@example.com",
                "09171234567",
                "QRT-9X2M4P",
                Instant.now().minus(1, ChronoUnit.DAYS)
        );

        EventRepository eventRepository = mock(EventRepository.class);
        RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        when(registrationRepository.findByEventIdAndTicketCodeForUpdate(eq(eventId), eq("QRT-9X2M4P")))
                .thenAnswer(inv -> Optional.of(sharedRegistration));
        when(registrationRepository.save(any(RegistrationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ScanTicketHandler handler = new ScanTicketHandler(
                eventRepository,
                registrationRepository,
                signer,
                ZoneId.of("Asia/Manila")
        );

        String signedQr = signer.sign(eventId, "QRT-9X2M4P");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<ScanTicketResponse> scanTask = () -> {
            ready.countDown();
            start.await();
            synchronized (sharedRegistration) {
                return handler.handle(eventId, new ScanTicketRequest(signedQr), staff);
            }
        };

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<ScanTicketResponse> f1 = pool.submit(scanTask);
            Future<ScanTicketResponse> f2 = pool.submit(scanTask);
            ready.await();
            start.countDown();

            ScanTicketResponse r1 = f1.get();
            ScanTicketResponse r2 = f2.get();

            long checkedInCount = java.util.stream.Stream.of(r1, r2)
                    .filter(r -> r.outcome() == CheckInOutcome.CHECKED_IN)
                    .count();
            long alreadyCount = java.util.stream.Stream.of(r1, r2)
                    .filter(r -> r.outcome() == CheckInOutcome.ALREADY_CHECKED_IN)
                    .count();

            assertEquals(1, checkedInCount);
            assertEquals(1, alreadyCount);
        }
    }
}