import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  AlertCircle,
  ArrowUpCircle,
  CalendarCheck,
  CalendarPlus,
  CheckCircle2,
  History,
  Lock,
  RotateCcw,
  XCircle,
} from 'lucide-react';
import { formatDateTime } from '@/shared/lib/dateTime';
import { auditApi } from './auditApi';
import type { AuditAction } from './audit.types';

interface AuditTrailPanelProps {
  events: Array<{ id: string; title: string }>;
}

function ActionBadge({ action }: { action: AuditAction }) {
  switch (action) {
    case 'TICKET_CHECKED_IN':
      return (
        <span className="inline-flex items-center gap-1 rounded-full border border-emerald-200 bg-emerald-50 px-2 py-0.5 text-[11px] font-semibold text-emerald-700">
          <CheckCircle2 className="h-3 w-3" />
          Check-in
        </span>
      );
    case 'CHECKIN_UNDONE':
      return (
        <span className="inline-flex items-center gap-1 rounded-full border border-amber-200 bg-amber-50 px-2 py-0.5 text-[11px] font-semibold text-amber-800">
          <RotateCcw className="h-3 w-3" />
          Undo Check-in
        </span>
      );
    case 'WAITLIST_PROMOTED':
      return (
        <span className="inline-flex items-center gap-1 rounded-full border border-sky-200 bg-sky-50 px-2 py-0.5 text-[11px] font-semibold text-sky-700">
          <ArrowUpCircle className="h-3 w-3" />
          Promoted
        </span>
      );
    case 'REGISTRATION_CANCELLED':
      return (
        <span className="inline-flex items-center gap-1 rounded-full border border-red-200 bg-red-50 px-2 py-0.5 text-[11px] font-semibold text-red-700">
          <XCircle className="h-3 w-3" />
          Cancelled
        </span>
      );
    case 'EVENT_CLOSED':
      return (
        <span className="inline-flex items-center gap-1 rounded-full border border-slate-300 bg-slate-100 px-2 py-0.5 text-[11px] font-semibold text-slate-700">
          <Lock className="h-3 w-3" />
          Event Closed
        </span>
      );
    case 'EVENT_CREATED':
      return (
        <span className="inline-flex items-center gap-1 rounded-full border border-indigo-200 bg-indigo-50 px-2 py-0.5 text-[11px] font-semibold text-indigo-700">
          <CalendarPlus className="h-3 w-3" />
          Event Created
        </span>
      );
    default:
      return (
        <span className="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-slate-50 px-2 py-0.5 text-[11px] font-semibold text-slate-700">
          <CalendarCheck className="h-3 w-3" />
          Event Updated
        </span>
      );
  }
}

export function AuditTrailPanel({ events }: AuditTrailPanelProps) {
  const [selectedEventId, setSelectedEventId] = useState<string>('');

  const { data: logs, isLoading, isError } = useQuery({
    queryKey: ['staff-audit-logs', selectedEventId],
    queryFn: () => auditApi.listRecent(selectedEventId || undefined, 25),
    refetchInterval: 10000,
  });

  return (
    <div className="space-y-4 rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div className="flex items-center gap-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-700">
            <History className="h-4 w-4" />
          </span>
          <div>
            <h2 className="text-base font-bold text-slate-900">Audit Trail</h2>
            <p className="text-xs text-slate-500">
              Immutable activity log for event changes, check-ins, cancellations, and waitlist promotions.
            </p>
          </div>
        </div>

        <select
          aria-label="Filter audit logs by event"
          value={selectedEventId}
          onChange={(e) => setSelectedEventId(e.target.value)}
          className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 focus:border-slate-900 focus:outline-none"
        >
          <option value="">All Events</option>
          {events.map((ev) => (
            <option key={ev.id} value={ev.id}>
              {ev.title}
            </option>
          ))}
        </select>
      </div>

      {isError && (
        <div className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-800">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span>Unable to load audit logs.</span>
        </div>
      )}

      {isLoading ? (
        <div className="space-y-2">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-12 animate-pulse rounded-lg bg-slate-100" />
          ))}
        </div>
      ) : (logs ?? []).length === 0 ? (
        <p className="rounded-lg border border-dashed border-slate-200 py-6 text-center text-xs text-slate-500">
          No recorded actions yet.
        </p>
      ) : (
        <div className="divide-y divide-slate-100">
          {(logs ?? []).map((log) => (
            <div
              key={log.id}
              className="flex flex-col justify-between gap-2 py-3 text-xs sm:flex-row sm:items-center"
            >
              <div className="space-y-1">
                <div className="flex flex-wrap items-center gap-2">
                  <ActionBadge action={log.action} />
                  <span className="font-medium text-slate-900">{log.summary}</span>
                </div>
                <div className="flex flex-wrap items-center gap-2 text-[11px] text-slate-500">
                  <span>
                    By <strong className="font-semibold text-slate-700">{log.actorLabel}</strong> ({log.actorType})
                  </span>
                  {log.eventTitle && (
                    <>
                      <span>·</span>
                      <span>{log.eventTitle}</span>
                    </>
                  )}
                </div>
              </div>

              <span className="shrink-0 font-mono text-[11px] text-slate-500">
                {formatDateTime(log.createdAt)}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}