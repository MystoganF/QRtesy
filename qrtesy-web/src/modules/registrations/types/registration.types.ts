export interface RegisterAttendeePayload {
  eventId: string;
  fullName: string;
  email: string;
  mobile: string;
}

export interface RegistrationTicket {
  registrationId: string;
  ticketCode: string;
  fullName: string;
  email: string;
  mobile: string;
  status: 'CONFIRMED' | 'CHECKED_IN' | 'WAITLISTED' | 'CANCELLED';
  registeredAt: string;
  checkedInAt: string | null;
  eventId: string;
  eventTitle: string;
  eventVenue: string;
  eventStartsAt: string;
  eventEndsAt: string;
  timezone: string;
}