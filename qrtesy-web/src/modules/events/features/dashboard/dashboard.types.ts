import type { EventStatus } from "../../types/event.types";

export interface DashboardEventSummary {
  id: string;
  title: string;
  venue: string;
  startsAt: string;
  endsAt: string;
  timezone: string;
  status: EventStatus;
  capacity: number;
  confirmedCount: number;
  checkedInCount: number;
  occupiedCount: number;
  remainingSpots: number;
  waitlistCount: number;
  cancelledCount: number;
  checkInRatePercent: number;
}

export interface StaffDashboardResponse {
  totalEvents: number;
  publishedEvents: number;
  draftEvents: number;
  closedEvents: number;
  totalCapacity: number;
  totalOccupied: number;
  totalCheckedIn: number;
  totalWaitlisted: number;
  overallCheckInRatePercent: number;
  events: DashboardEventSummary[];
}