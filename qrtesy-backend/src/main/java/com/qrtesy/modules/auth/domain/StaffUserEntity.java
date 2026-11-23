package com.qrtesy.modules.auth.domain;

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
@Table(name = "staff_users", schema = "qrtesy")
public class StaffUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StaffRole role = StaffRole.ORGANIZER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StaffUserEntity() {
    }

    public static StaffUserEntity create(
            String name,
            String username,
            String passwordHash,
            StaffRole role,
            Instant createdAt
    ) {
        StaffUserEntity entity = new StaffUserEntity();
        entity.name = name;
        entity.username = username;
        entity.passwordHash = passwordHash;
        entity.role = role;
        entity.createdAt = createdAt;
        return entity;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public StaffRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}