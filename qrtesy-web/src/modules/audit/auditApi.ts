import { apiClient } from '@/app/apiClient';
import type { AuditLogItem } from './audit.types';

export const auditApi = {
  listRecent: (eventId?: string, limit = 25) => {
    const params = new URLSearchParams();
    if (eventId) {
      params.set('eventId', eventId);
    }
    params.set('limit', String(limit));
    return apiClient.get<AuditLogItem[]>(`/api/staff/audit-logs?${params.toString()}`);
  },
};