export interface AttendeeItem {
  registrationId: string;
  ticketCode: string;
  fullName: string;
  email: string;
  mobile: string;
  status: 'CONFIRMED' | 'CHECKED_IN' | 'WAITLISTED' | 'CANCELLED';
  registeredAt: string;
  checkedInAt: string | null;
}

export interface EventAttendeesResponse {
  eventId: string;
  eventTitle: string;
  eventVenue: string;
  eventStartsAt: string;
  eventEndsAt: string;
  timezone: string;
  capacity: number;
  confirmedCount: number;
  checkedInCount: number;
  totalOccupied: number;
  remainingSpots: number;
  attendees: AttendeeItem[];
}