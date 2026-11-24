package com.qrtesy.shared.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TicketQrSigner {

    private static final String TOKEN_PREFIX = "QRT1";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int SIGNATURE_BYTES = 12;

    private final byte[] secretBytes;

    public TicketQrSigner(@Value("${app.jwt.secret}") String secret) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String sign(UUID eventId, String ticketCode) {
        String normalizedCode = ticketCode.trim().toUpperCase(Locale.ROOT);
        String signature = computeSignature(eventId, normalizedCode);
        return TOKEN_PREFIX + "." + normalizedCode + "." + signature;
    }

    public VerificationResult verifyAndExtract(UUID expectedEventId, String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return VerificationResult.invalid("Ticket code is required.");
        }

        String trimmed = rawInput.trim();
        if (trimmed.startsWith(TOKEN_PREFIX + ".")) {
            String[] parts = trimmed.split("\\.");
            if (parts.length != 3 || parts[1].isBlank() || parts[2].isBlank()) {
                return VerificationResult.invalid("Invalid QR token format.");
            }

            String candidateCode = parts[1].trim().toUpperCase(Locale.ROOT);
            String providedSig = parts[2].trim();
            String expectedSig = computeSignature(expectedEventId, candidateCode);

            boolean valid = MessageDigest.isEqual(
                    expectedSig.getBytes(StandardCharsets.UTF_8),
                    providedSig.getBytes(StandardCharsets.UTF_8)
            );

            if (!valid) {
                return VerificationResult.invalid("Invalid or tampered QR ticket.");
            }
            return VerificationResult.valid(candidateCode, true);
        }

        // Manual entry fallback: human-readable SecureRandom ticket code
        return VerificationResult.valid(trimmed.toUpperCase(Locale.ROOT), false);
    }

    private String computeSignature(UUID eventId, String normalizedCode) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALGORITHM));
            String payload = eventId + ":" + normalizedCode;
            byte[] fullHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            byte[] truncated = Arrays.copyOf(fullHmac, SIGNATURE_BYTES);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(truncated);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to compute QR HMAC signature.", ex);
        }
    }

    public record VerificationResult(
            boolean valid,
            String ticketCode,
            boolean signedToken,
            String errorMessage
    ) {
        public static VerificationResult valid(String ticketCode, boolean signedToken) {
            return new VerificationResult(true, ticketCode, signedToken, null);
        }

        public static VerificationResult invalid(String errorMessage) {
            return new VerificationResult(false, null, true, errorMessage);
        }
    }
}