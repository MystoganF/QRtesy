import { apiClient } from '@/app/apiClient';
import type { ScanTicketResponse } from './checkin.types';

export const checkinApi = {
  scanTicket: (eventId: string, ticketCode: string) =>
    apiClient.post<ScanTicketResponse>(`/api/staff/events/${eventId}/check-in`, {
      ticketCode: ticketCode.trim(),
    }),
};