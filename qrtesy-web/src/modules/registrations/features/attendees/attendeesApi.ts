import { apiClient } from '@/app/apiClient';
import type { EventAttendeesResponse } from './attendees.types';

export interface UndoCheckInResponse {
  registrationId: string;
  eventId: string;
  ticketCode: string;
  fullName: string;
  status: string;
  message: string;
}

export const attendeesApi = {
  listByEvent: (eventId: string, query?: string) => {
    const params = new URLSearchParams();
    if (query && query.trim().length > 0) {
      params.set('q', query.trim());
    }
    const suffix = params.toString() ? `?${params.toString()}` : '';
    return apiClient.get<EventAttendeesResponse>(
      `/api/staff/events/${encodeURIComponent(eventId)}/attendees${suffix}`
    );
  },

  undoCheckIn: (registrationId: string) =>
    apiClient.post<UndoCheckInResponse>(
      `/api/staff/registrations/${encodeURIComponent(registrationId)}/undo-check-in`
    ),

  exportCsv: async (eventId: string, query?: string) => {
    const params = new URLSearchParams();
    if (query && query.trim().length > 0) {
      params.set('q', query.trim());
    }
    const suffix = params.toString() ? `?${params.toString()}` : '';
    const baseUrl = import.meta.env.VITE_API_BASE_URL ?? '';

    const response = await fetch(
      `${baseUrl}/api/staff/events/${encodeURIComponent(eventId)}/attendees/export${suffix}`,
      {
        method: 'GET',
        credentials: 'include',
      }
    );

    if (!response.ok) {
      throw new Error('Failed to export CSV.');
    }

    const disposition = response.headers.get('Content-Disposition') ?? '';
    const match = disposition.match(/filename="?([^";]+)"?/i);
    const filename = match?.[1] ?? `attendees-${eventId}.csv`;

    const blob = await response.blob();
    const downloadUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(downloadUrl);
  },
};