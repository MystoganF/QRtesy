package com.qrtesy.modules.notifications.domain;

import com.qrtesy.modules.events.domain.EventEntity;
import com.qrtesy.modules.registrations.domain.RegistrationEntity;
import com.qrtesy.modules.registrations.domain.RegistrationStatus;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final URI RESEND_ENDPOINT = URI.create("https://api.resend.com/emails");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private final ZoneId manilaZoneId;
    private final HttpClient httpClient;
    private final boolean mailEnabled;
    private final String resendApiKey;
    private final String fromAddress;
    private final String publicBaseUrl;

    public EmailNotificationService(
            ZoneId manilaZoneId,
            @Value("${app.mail.enabled:true}") boolean mailEnabled,
            @Value("${app.mail.resend-api-key:}") String resendApiKey,
            @Value("${app.mail.from:QRtesy <onboarding@resend.dev>}") String fromAddress,
            @Value("${app.public-base-url:http://localhost:5173}") String publicBaseUrl
    ) {
        this.manilaZoneId = manilaZoneId;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
        this.mailEnabled = mailEnabled;
        this.resendApiKey = resendApiKey != null ? resendApiKey.trim() : "";
        this.fromAddress = fromAddress;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    public void sendRegistrationConfirmationAfterCommit(
            EventEntity event,
            RegistrationEntity registration,
            Integer waitlistPosition
    ) {
        String ticketUrl = buildTicketLookupUrl(registration.getTicketCode());
        String formattedDate = formatDate(event);
        String formattedTime = formatTimeRange(event);
        boolean waitlisted = registration.getStatus() == RegistrationStatus.WAITLISTED;

        String subject = waitlisted
                ? "Waitlist Confirmation (#" + (waitlistPosition != null ? waitlistPosition : 1) + "): " + event.getTitle()
                : "Your QR Ticket Pass: " + event.getTitle() + " (" + registration.getTicketCode() + ")";

        String statusHeadline = waitlisted
                ? "You are #" + (waitlistPosition != null ? waitlistPosition : 1) + " on the waitlist"
                : "Your registration is confirmed";

        String statusNote = waitlisted
                ? "All confirmed seats are currently filled. If a seat opens, we will promote you automatically in order and email your confirmed QR pass."
                : "Present your QR ticket pass at the venue entrance for fast door check-in.";

        String html = buildEmailHtml(
                registration.getFullName(),
                statusHeadline,
                statusNote,
                event.getTitle(),
                formattedDate,
                formattedTime,
                event.getVenue(),
                registration.getTicketCode(),
                ticketUrl,
                waitlisted ? "Check Waitlist Status" : "Open QR Ticket Pass"
        );

        String text = buildEmailText(
                registration.getFullName(),
                statusHeadline,
                statusNote,
                event.getTitle(),
                formattedDate,
                formattedTime,
                event.getVenue(),
                registration.getTicketCode(),
                ticketUrl
        );

        runAfterCommit(() -> dispatchEmail(registration.getEmail(), subject, html, text));
    }

    public void sendWaitlistPromotionAfterCommit(EventEntity event, RegistrationEntity registration) {
        String ticketUrl = buildTicketLookupUrl(registration.getTicketCode());
        String formattedDate = formatDate(event);
        String formattedTime = formatTimeRange(event);

        String subject = "Spot Opened! Your QR Ticket is Confirmed: "
                + event.getTitle() + " (" + registration.getTicketCode() + ")";

        String statusHeadline = "You've been promoted from the waitlist!";
        String statusNote = "A spot opened up and your registration is now Confirmed. Present your QR ticket pass at the entrance.";

        String html = buildEmailHtml(
                registration.getFullName(),
                statusHeadline,
                statusNote,
                event.getTitle(),
                formattedDate,
                formattedTime,
                event.getVenue(),
                registration.getTicketCode(),
                ticketUrl,
                "Open Confirmed QR Pass"
        );

        String text = buildEmailText(
                registration.getFullName(),
                statusHeadline,
                statusNote,
                event.getTitle(),
                formattedDate,
                formattedTime,
                event.getVenue(),
                registration.getTicketCode(),
                ticketUrl
        );

        runAfterCommit(() -> dispatchEmail(registration.getEmail(), subject, html, text));
    }

    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private void dispatchEmail(String to, String subject, String html, String text) {
        if (!mailEnabled) {
            return;
        }

        if (resendApiKey.isEmpty()) {
            log.info("[MAIL SANDBOX DRY-RUN] To: {} | Subject: {} | Body:\n{}", to, subject, text);
            return;
        }

        try {
            String jsonBody = buildResendJsonPayload(fromAddress, to, subject, html, text);

            HttpRequest request = HttpRequest.newBuilder(RESEND_ENDPOINT)
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Sent email '{}' to {}", subject, to);
            } else {
                log.warn("Resend API returned status {} for recipient {}: {}",
                        response.statusCode(), to, response.body());
            }
        } catch (Exception ex) {
            log.warn("Failed to dispatch email '{}' to {}: {}", subject, to, ex.getMessage());
        }
    }

    private String buildResendJsonPayload(
            String from,
            String to,
            String subject,
            String html,
            String text
    ) {
        return "{"
                + "\"from\":" + jsonString(from) + ","
                + "\"to\":[" + jsonString(to) + "],"
                + "\"subject\":" + jsonString(subject) + ","
                + "\"html\":" + jsonString(html) + ","
                + "\"text\":" + jsonString(text)
                + "}";
    }

    private String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private String buildTicketLookupUrl(String ticketCode) {
        String encoded = URLEncoder.encode(ticketCode, StandardCharsets.UTF_8);
        return publicBaseUrl + "/ticket/lookup?code=" + encoded;
    }

    private String formatDate(EventEntity event) {
        return event.getStartsAt().atZone(manilaZoneId).format(DATE_FMT);
    }

    private String formatTimeRange(EventEntity event) {
        ZonedDateTime start = event.getStartsAt().atZone(manilaZoneId);
        ZonedDateTime end = event.getEndsAt().atZone(manilaZoneId);
        return start.format(TIME_FMT) + " – " + end.format(TIME_FMT) + " (PHT)";
    }

    private String buildEmailHtml(
            String attendeeName,
            String headline,
            String note,
            String eventTitle,
            String dateStr,
            String timeStr,
            String venue,
            String ticketCode,
            String ticketUrl,
            String ctaLabel
    ) {
        return """
                <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 520px; margin: 0 auto; padding: 24px; color: #0f172a;">
                  <div style="border: 1px solid #e2e8f0; border-radius: 14px; overflow: hidden; background: #ffffff;">
                    <div style="background: #0f172a; color: #ffffff; padding: 20px 24px;">
                      <p style="margin: 0; font-size: 11px; letter-spacing: 0.08em; text-transform: uppercase; color: #94a3b8;">QRtesy Event Pass</p>
                      <h1 style="margin: 6px 0 0; font-size: 20px; line-height: 1.3;">%s</h1>
                    </div>
                    <div style="padding: 24px;">
                      <p style="margin: 0 0 8px; font-size: 15px;">Hi <strong>%s</strong>,</p>
                      <p style="margin: 0 0 16px; font-size: 14px; color: #475569; line-height: 1.5;"><strong>%s.</strong> %s</p>
                      <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px; margin-bottom: 20px;">
                        <p style="margin: 0 0 6px; font-size: 12px; color: #64748b;">TICKET CODE</p>
                        <p style="margin: 0 0 14px; font-family: monospace; font-size: 20px; font-weight: 700; letter-spacing: 0.1em; color: #0f172a;">%s</p>
                        <p style="margin: 0 0 4px; font-size: 13px; color: #334155;"><strong>Date:</strong> %s</p>
                        <p style="margin: 0 0 4px; font-size: 13px; color: #334155;"><strong>Time:</strong> %s</p>
                        <p style="margin: 0; font-size: 13px; color: #334155;"><strong>Venue:</strong> %s</p>
                      </div>
                      <a href="%s" style="display: inline-block; background: #0f172a; color: #ffffff; text-decoration: none; font-weight: 600; font-size: 14px; padding: 12px 20px; border-radius: 8px;">%s</a>
                      <p style="margin: 16px 0 0; font-size: 12px; color: #64748b; word-break: break-all;">Or open this link: %s</p>
                    </div>
                  </div>
                </div>
                """.formatted(
                escapeHtml(eventTitle),
                escapeHtml(attendeeName),
                escapeHtml(headline),
                escapeHtml(note),
                escapeHtml(ticketCode),
                escapeHtml(dateStr),
                escapeHtml(timeStr),
                escapeHtml(venue),
                escapeHtml(ticketUrl),
                escapeHtml(ctaLabel),
                escapeHtml(ticketUrl)
        );
    }

    private String buildEmailText(
            String attendeeName,
            String headline,
            String note,
            String eventTitle,
            String dateStr,
            String timeStr,
            String venue,
            String ticketCode,
            String ticketUrl
    ) {
        return """
                Hi %s,

                %s. %s

                Event: %s
                Ticket Code: %s
                Date: %s
                Time: %s
                Venue: %s

                View your QR ticket pass: %s
                """.formatted(
                attendeeName,
                headline,
                note,
                eventTitle,
                ticketCode,
                dateStr,
                timeStr,
                venue,
                ticketUrl
        );
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}