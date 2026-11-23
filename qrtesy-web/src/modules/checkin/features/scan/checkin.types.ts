export type CheckInOutcome =
  | 'CHECKED_IN'
  | 'ALREADY_CHECKED_IN'
  | 'INVALID_TICKET'
  | 'NOT_CONFIRMED';

export interface ScanTicketResponse {
  outcome: CheckInOutcome;
  message: string;
  registrationId: string | null;
  ticketCode: string;
  fullName: string | null;
  status: string | null;
  checkedInAt: string | null;
  timezone: string;
}