package com.qrtesy.modules.registrations.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registrations", schema = "qrtesy")
public class RegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 20)
    private String mobile;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 32)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "checked_in_by")
    private UUID checkedInBy;

    protected RegistrationEntity() {
    }

    public static RegistrationEntity createConfirmed(
            UUID eventId,
            String fullName,
            String email,
            String mobile,
            String ticketCode,
            Instant registeredAt
    ) {
        RegistrationEntity entity = new RegistrationEntity();
        entity.eventId = eventId;
        entity.fullName = fullName;
        entity.email = email;
        entity.mobile = mobile;
        entity.ticketCode = ticketCode;
        entity.status = RegistrationStatus.CONFIRMED;
        entity.registeredAt = registeredAt;
        return entity;
    }

    public static RegistrationEntity createWaitlisted(
            UUID eventId,
            String fullName,
            String email,
            String mobile,
            String ticketCode,
            Instant registeredAt
    ) {
        RegistrationEntity entity = new RegistrationEntity();
        entity.eventId = eventId;
        entity.fullName = fullName;
        entity.email = email;
        entity.mobile = mobile;
        entity.ticketCode = ticketCode;
        entity.status = RegistrationStatus.WAITLISTED;
        entity.registeredAt = registeredAt;
        return entity;
    }

    public void promoteToConfirmed() {
        if (this.status == RegistrationStatus.WAITLISTED) {
            this.status = RegistrationStatus.CONFIRMED;
        }
    }

    public void markCheckedIn(Instant checkedInAt, UUID staffUserId) {
        this.status = RegistrationStatus.CHECKED_IN;
        this.checkedInAt = checkedInAt;
        this.checkedInBy = staffUserId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }

    public UUID getCheckedInBy() {
        return checkedInBy;
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
    }
}