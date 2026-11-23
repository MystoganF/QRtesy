import { apiClient } from '@/app/apiClient';
import type {
  EventDetails,
  PublicEventSummary,
  SaveEventPayload,
  StaffEventItem,
} from '../type/event.types';

export const eventsApi = {
  listPublic: () => apiClient.get<PublicEventSummary[]>('/api/events'),
  getDetails: (eventId: string) => apiClient.get<EventDetails>(`/api/events/${eventId}`),

  listStaffAll: () => apiClient.get<StaffEventItem[]>('/api/staff/events'),
  createEvent: (payload: SaveEventPayload) =>
    apiClient.post<StaffEventItem>('/api/staff/events', payload),
  updateEvent: (eventId: string, payload: SaveEventPayload) =>
    apiClient.put<StaffEventItem>(`/api/staff/events/${eventId}`, payload),
  closeEvent: (eventId: string) =>
    apiClient.patch<StaffEventItem>(`/api/staff/events/${eventId}/close`),
};