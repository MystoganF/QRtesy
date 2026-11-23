export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED';

export interface PublicEventSummary {
  id: string;
  title: string;
  description: string;
  venue: string;
  startsAt: string;
  endsAt: string;
  timezone: string;
  capacity: number;
  confirmedCount: number;
  remainingSpots: number;
  full: boolean;
}

export interface EventDetails extends PublicEventSummary {
  status: EventStatus;
}

export interface StaffEventItem {
  id: string;
  title: string;
  description: string;
  venue: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  startsAt: string;
  endsAt: string;
  timezone: string;
  capacity: number;
  occupiedCount: number;
  remainingSpots: number;
  status: EventStatus;
}

export interface SaveEventPayload {
  title: string;
  description: string;
  venue: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  capacity: number;
  status: EventStatus;
}