import { apiClient } from '@/app/apiClient';
import type { RegisterAttendeePayload, RegistrationTicket } from '../types/registration.types';

export const registrationsApi = {
  register: (payload: RegisterAttendeePayload) =>
    apiClient.post<RegistrationTicket>('/api/registrations', payload),

  lookup: (ticketCode: string, email: string) =>
    apiClient.get<RegistrationTicket>(
      `/api/registrations/${encodeURIComponent(ticketCode.trim())}?email=${encodeURIComponent(email.trim())}`
    ),
};