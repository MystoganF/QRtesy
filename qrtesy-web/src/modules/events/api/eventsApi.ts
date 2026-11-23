import { apiClient } from '@/app/apiClient';
import type { EventDetails, PublicEventSummary } from '../type/event.types';

export const eventsApi = {
  listPublic: () => apiClient.get<PublicEventSummary[]>('/api/events'),
  getDetails: (eventId: string) => apiClient.get<EventDetails>(`/api/events/${eventId}`),
};