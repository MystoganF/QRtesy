package com.qrtesy.shared.security;

import jakarta.servlet.DispatcherType;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtCookieFilter jwtCookieFilter;
    private final RateLimitFilter rateLimitFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtCookieFilter jwtCookieFilter,
            RateLimitFilter rateLimitFilter,
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String originsCsv) {
        this.jwtCookieFilter = jwtCookieFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.allowedOrigins = Arrays.stream(originsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()
                        .requestMatchers("/api/events/**").permitAll()
                        .requestMatchers("/api/registrations/**").permitAll()
                        // Organizer-only event management, registration administration, and audit logs
                        .requestMatchers(HttpMethod.POST, "/api/staff/events").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.PUT, "/api/staff/events/**").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.PATCH, "/api/staff/events/**").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/staff/events/**").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/staff/events/*/close").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/staff/registrations/*/cancel").hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/staff/registrations/*/promote").hasRole("ORGANIZER")
                        .requestMatchers("/api/staff/audit-logs/**").hasRole("ORGANIZER")
                        // Shared operational endpoints (Organizer + Door Staff)
                        .requestMatchers(HttpMethod.GET, "/api/staff/events").hasAnyRole("ORGANIZER", "DOOR_STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/staff/events/*/attendees/**").hasAnyRole("ORGANIZER", "DOOR_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/staff/events/*/checkin").hasAnyRole("ORGANIZER", "DOOR_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/staff/registrations/*/undo-checkin").hasAnyRole("ORGANIZER", "DOOR_STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/staff/dashboard").hasAnyRole("ORGANIZER", "DOOR_STAFF")
                        .requestMatchers("/api/staff/**").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, JwtCookieFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}