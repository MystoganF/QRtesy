import { apiClient } from '@/app/apiClient';
import type { LoginPayload, StaffUser } from '../types/auth.types';

export const authApi = {
  login: (payload: LoginPayload) => apiClient.post<StaffUser>('/api/auth/login', payload),
  logout: () => apiClient.post<{ message: string }>('/api/auth/logout'),
  me: () => apiClient.get<StaffUser>('/api/auth/me'),
};