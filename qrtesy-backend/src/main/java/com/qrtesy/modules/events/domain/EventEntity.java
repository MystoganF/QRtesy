package com.qrtesy.modules.events.domain;

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
@Table(name = "events", schema = "qrtesy")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 200)
    private String venue;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.PUBLISHED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EventEntity() {
    }

    public static EventEntity create(
            String title,
            String description,
            String venue,
            Instant startsAt,
            Instant endsAt,
            int capacity,
            EventStatus status,
            Instant now
    ) {
        EventEntity entity = new EventEntity();
        entity.title = title;
        entity.description = description;
        entity.venue = venue;
        entity.startsAt = startsAt;
        entity.endsAt = endsAt;
        entity.capacity = capacity;
        entity.status = status;
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public void update(
            String title,
            String description,
            String venue,
            Instant startsAt,
            Instant endsAt,
            int capacity,
            EventStatus status,
            Instant now
    ) {
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.capacity = capacity;
        this.status = status;
        this.updatedAt = now;
    }

    public void close(Instant now) {
        this.status = EventStatus.CLOSED;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVenue() {
        return venue;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public EventStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}