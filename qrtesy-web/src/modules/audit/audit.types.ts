export type AuditActorType = 'STAFF' | 'ATTENDEE' | 'SYSTEM';

export type AuditAction =
  | 'EVENT_CREATED'
  | 'EVENT_UPDATED'
  | 'EVENT_CLOSED'
  | 'TICKET_CHECKED_IN'
  | 'CHECKIN_UNDONE'
  | 'REGISTRATION_CANCELLED'
  | 'WAITLIST_PROMOTED';

export interface AuditLogItem {
  id: string;
  eventId: string | null;
  eventTitle: string | null;
  registrationId: string | null;
  actorType: AuditActorType;
  actorLabel: string;
  action: AuditAction;
  summary: string;
  createdAt: string;
  timezone: string;
}