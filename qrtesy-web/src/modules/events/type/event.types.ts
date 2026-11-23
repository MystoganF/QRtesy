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
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED';
}