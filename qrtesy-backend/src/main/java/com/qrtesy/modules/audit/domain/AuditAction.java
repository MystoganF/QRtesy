package com.qrtesy.modules.audit.domain;

public enum AuditAction {
    EVENT_CREATED,
    EVENT_UPDATED,
    EVENT_CLOSED,
    TICKET_CHECKED_IN,
    CHECKIN_UNDONE,
    REGISTRATION_CANCELLED,
    WAITLIST_PROMOTED
}