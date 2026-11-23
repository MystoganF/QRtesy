import { apiClient } from '@/app/apiClient';
import type { EventAttendeesResponse } from './attendees.types';

export const attendeesApi = {
  listByEvent: (eventId: string, query?: string) => {
    const trimmed = query?.trim();
    const qs = trimmed ? `?q=${encodeURIComponent(trimmed)}` : '';
    return apiClient.get<EventAttendeesResponse>(`/api/staff/events/${eventId}/attendees${qs}`);
  },
};