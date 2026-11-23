CREATE SCHEMA IF NOT EXISTS qrtesy;

CREATE TABLE qrtesy.staff_users (
                                    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    name          VARCHAR(120) NOT NULL,
                                    username      VARCHAR(60)  NOT NULL UNIQUE,
                                    password_hash VARCHAR(255) NOT NULL,
                                    role          VARCHAR(20)  NOT NULL DEFAULT 'ORGANIZER'
                                        CHECK (role IN ('ORGANIZER', 'DOOR_STAFF')),
                                    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE qrtesy.events (
                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               title       VARCHAR(160) NOT NULL,
                               description TEXT         NOT NULL,
                               venue       VARCHAR(200) NOT NULL,
                               starts_at   TIMESTAMPTZ  NOT NULL,
                               ends_at     TIMESTAMPTZ  NOT NULL,
                               capacity    INTEGER      NOT NULL CHECK (capacity >= 1),
                               status      VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED'
                                   CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED')),
                               created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                               updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                               CONSTRAINT chk_events_time_order CHECK (ends_at > starts_at)
);

CREATE TABLE qrtesy.registrations (
                                      id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      event_id      UUID         NOT NULL REFERENCES qrtesy.events(id) ON DELETE RESTRICT,
                                      full_name     VARCHAR(120) NOT NULL,
                                      email         VARCHAR(160) NOT NULL,
                                      mobile        VARCHAR(11)  NOT NULL CHECK (mobile ~ '^09[0-9]{9}$'),
    ticket_code   VARCHAR(32)  NOT NULL UNIQUE,
    status        VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED'
                  CHECK (status IN ('CONFIRMED', 'CHECKED_IN', 'WAITLISTED', 'CANCELLED')),
    registered_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    checked_in_at TIMESTAMPTZ  NULL,
    checked_in_by UUID         NULL REFERENCES qrtesy.staff_users(id)
);

-- Prevents duplicate active registration for the same email + event; allows re-registering if CANCELLED
CREATE UNIQUE INDEX uq_registrations_active_email_per_event
    ON qrtesy.registrations (event_id, LOWER(email))
    WHERE status IN ('CONFIRMED', 'CHECKED_IN', 'WAITLISTED');

CREATE INDEX idx_registrations_event_status ON qrtesy.registrations (event_id, status);
CREATE INDEX idx_registrations_event_code   ON qrtesy.registrations (event_id, ticket_code);
CREATE INDEX idx_events_status_starts_at    ON qrtesy.events (status, starts_at);