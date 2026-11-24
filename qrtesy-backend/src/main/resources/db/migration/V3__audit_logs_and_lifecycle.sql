CREATE TABLE qrtesy.audit_logs (
                                   id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   event_id        UUID         NULL REFERENCES qrtesy.events(id) ON DELETE SET NULL,
                                   registration_id UUID         NULL REFERENCES qrtesy.registrations(id) ON DELETE SET NULL,
                                   actor_type      VARCHAR(20)  NOT NULL CHECK (actor_type IN ('STAFF', 'ATTENDEE', 'SYSTEM')),
                                   actor_id        UUID         NULL,
                                   actor_label     VARCHAR(120) NOT NULL,
                                   action          VARCHAR(40)  NOT NULL,
                                   summary         TEXT         NOT NULL,
                                   created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_created_at ON qrtesy.audit_logs (created_at DESC);
CREATE INDEX idx_audit_logs_event_created_at ON qrtesy.audit_logs (event_id, created_at DESC);