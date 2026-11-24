package com.qrtesy.shared.security;

import com.qrtesy.shared.security.JwtCookieFilter.StaffPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000L;

    private final int lookupLimitPerMinute;
    private final int checkInLimitPerMinute;
    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    public RateLimitFilter(
            @Value("${app.rate-limit.lookup-per-minute:15}") int lookupLimitPerMinute,
            @Value("${app.rate-limit.checkin-per-minute:45}") int checkInLimitPerMinute
    ) {
        this.lookupLimitPerMinute = lookupLimitPerMinute;
        this.checkInLimitPerMinute = checkInLimitPerMinute;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        RateRule rule = resolveRule(method, path, request);
        if (rule != null && !tryConsume(rule.bucketKey(), rule.maxRequests())) {
            writeRateLimitResponse(response, rule.message());
            return;
        }

        if (requestCounter.incrementAndGet() % 200 == 0) {
            pruneStaleBuckets();
        }

        filterChain.doFilter(request, response);
    }

    private RateRule resolveRule(String method, String path, HttpServletRequest request) {
        if (HttpMethod.GET.matches(method) && "/api/registrations/lookup".equals(path)) {
            return new RateRule(
                    "lookup:" + resolveClientIp(request),
                    lookupLimitPerMinute,
                    "Too many ticket lookup attempts. Please wait a minute before trying again."
            );
        }

        if (HttpMethod.POST.matches(method)
                && path.startsWith("/api/staff/events/")
                && path.endsWith("/check-in")) {
            return new RateRule(
                    "checkin:" + resolveStaffOrIp(request),
                    checkInLimitPerMinute,
                    "Check-in rate limit reached. Pause briefly before scanning again."
            );
        }

        return null;
    }

    private boolean tryConsume(String key, int maxRequests) {
        long now = System.currentTimeMillis();
        long cutoff = now - WINDOW_MILLIS;

        Deque<Long> timestamps = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private void pruneStaleBuckets() {
        long cutoff = System.currentTimeMillis() - WINDOW_MILLIS;
        buckets.entrySet().removeIf(entry -> {
            Deque<Long> q = entry.getValue();
            synchronized (q) {
                while (!q.isEmpty() && q.peekFirst() <= cutoff) {
                    q.pollFirst();
                }
                return q.isEmpty();
            }
        });
    }

    private String resolveStaffOrIp(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof StaffPrincipal staff) {
            return "staff:" + staff.id();
        }
        return "ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeRateLimitResponse(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", "60");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String body = """
                {
                  "type": "https://qrtesy.local/errors/rate-limit-exceeded",
                  "title": "Too Many Requests",
                  "status": 429,
                  "detail": "%s",
                  "code": "RATE_LIMIT_EXCEEDED"
                }
                """.formatted(detail.replace("\"", "\\\""));
        response.getWriter().write(body);
    }

    private record RateRule(String bucketKey, int maxRequests, String message) {}
}