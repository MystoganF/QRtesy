import { apiClient } from '@/app/apiClient';
import type { StaffDashboardResponse } from './dashboard.types';

export const dashboardApi = {
  getSummary: () => apiClient.get<StaffDashboardResponse>('/api/staff/dashboard'),
};