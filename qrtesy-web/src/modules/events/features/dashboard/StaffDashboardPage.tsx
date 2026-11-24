import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  AlertCircle,
  Calendar,
  CalendarCog,
  CheckCircle2,
  ClipboardList,
  Download,
  Hourglass,
  Loader2,
  MapPin,
  QrCode,
  Users,
} from 'lucide-react';
import { attendeesApi } from '@/modules/registrations/features/attendees/attendeesApi';
import { formatEventDate, formatTimeRange } from '@/shared/lib/dateTime';
import type { EventStatus } from '../../types/event.types';
import { dashboardApi } from './dashboardApi';

function StatusBadge({ status }: { status: EventStatus }) {
  if (status === 'PUBLISHED') {
    return (
      <span className="rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-700">
        Published
      </span>
    );
  }
  if (status === 'DRAFT') {
    return (
      <span className="rounded-full border border-amber-200 bg-amber-50 px-2.5 py-0.5 text-xs font-semibold text-amber-800">
        Draft
      </span>
    );
  }
  return (
    <span className="rounded-full border border-slate-300 bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-700">
      Closed
    </span>
  );
}

export function StaffDashboardPage() {
  const [exportingEventId, setExportingEventId] = useState<string | null>(null);
  const [exportError, setExportError] = useState<string | null>(null);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['staff-dashboard'],
    queryFn: dashboardApi.getSummary,
  });

  const handleExportCsv = async (eventId: string) => {
    setExportError(null);
    setExportingEventId(eventId);
    try {
      await attendeesApi.exportCsv(eventId);
    } catch {
      setExportError('Unable to export CSV right now. Please try again.');
    } finally {
      setExportingEventId(null);
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-28 animate-pulse rounded-xl border border-slate-200 bg-white p-5" />
          ))}
        </div>
        <div className="h-64 animate-pulse rounded-xl border border-slate-200 bg-white p-6" />
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="flex items-center gap-2.5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
        <AlertCircle className="h-5 w-5 shrink-0" />
        <span>Unable to load dashboard summary.</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-6 shadow-xs sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Operations Dashboard</h1>
          <p className="mt-1 text-sm text-slate-600">
            Live registration occupancy, door check-in progress, and CSV exports across all events.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Link
            to="/staff/checkin"
            className="inline-flex cursor-pointer items-center gap-2 rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-800"
          >
            <QrCode className="h-4 w-4" />
            <span>Open Door Check-in</span>
          </Link>
          <Link
            to="/staff/events"
            className="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100"
          >
            <CalendarCog className="h-4 w-4" />
            <span>Manage Events</span>
          </Link>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between">
            <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
              Total Events
            </p>
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100 text-slate-700">
              <Calendar className="h-4 w-4" />
            </span>
          </div>
          <p className="mt-2 text-2xl font-bold text-slate-900">{data.totalEvents}</p>
          <p className="mt-1 text-xs text-slate-500">
            {data.publishedEvents} published · {data.draftEvents} draft · {data.closedEvents} closed
          </p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between">
            <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
              Total Registered
            </p>
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100 text-slate-700">
              <Users className="h-4 w-4" />
            </span>
          </div>
          <p className="mt-2 text-2xl font-bold text-slate-900">
            {data.totalOccupied}{' '}
            <span className="text-sm font-normal text-slate-500">/ {data.totalCapacity}</span>
          </p>
          <p className="mt-1 text-xs text-slate-500">Confirmed + checked-in seats</p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between">
            <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
              Check-In Rate
            </p>
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-50 text-emerald-700">
              <CheckCircle2 className="h-4 w-4" />
            </span>
          </div>
          <p className="mt-2 text-2xl font-bold text-emerald-700">
            {data.overallCheckInRatePercent}%
          </p>
          <p className="mt-1 text-xs text-slate-500">
            {data.totalCheckedIn} of {data.totalOccupied} checked in
          </p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
          <div className="flex items-center justify-between">
            <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
              Active Waitlist
            </p>
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-amber-50 text-amber-700">
              <Hourglass className="h-4 w-4" />
            </span>
          </div>
          <p className="mt-2 text-2xl font-bold text-amber-700">{data.totalWaitlisted}</p>
          <p className="mt-1 text-xs text-slate-500">Queued across full events</p>
        </div>
      </div>

      {exportError && (
        <div
          role="alert"
          className="flex items-center gap-2.5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800"
        >
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>{exportError}</span>
        </div>
      )}

      <div className="space-y-4">
        <h2 className="text-base font-bold text-slate-900">Event Breakdown</h2>

        {data.events.length === 0 ? (
          <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500">
            No events created yet.
          </div>
        ) : (
          data.events.map((ev) => {
            const fillPercent =
              ev.capacity > 0 ? Math.min(100, Math.round((ev.occupiedCount / ev.capacity) * 100)) : 0;

            return (
              <div
                key={ev.id}
                className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-4"
              >
                <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-start">
                  <div className="space-y-1.5">
                    <div className="flex flex-wrap items-center gap-2.5">
                      <h3 className="text-base font-bold text-slate-900">{ev.title}</h3>
                      <StatusBadge status={ev.status} />
                      {ev.waitlistCount > 0 && (
                        <span className="rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-semibold text-amber-800">
                          +{ev.waitlistCount} waitlisted
                        </span>
                      )}
                    </div>

                    <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-600">
                      <span className="inline-flex items-center gap-1.5">
                        <Calendar className="h-3.5 w-3.5 text-slate-400" />
                        {formatEventDate(ev.startsAt)} · {formatTimeRange(ev.startsAt, ev.endsAt)}
                      </span>
                      <span className="inline-flex items-center gap-1.5">
                        <MapPin className="h-3.5 w-3.5 text-slate-400" />
                        {ev.venue}
                      </span>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    <Link
                      to={`/staff/events/${ev.id}/attendees`}
                      className="inline-flex cursor-pointer items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-100"
                    >
                      <ClipboardList className="h-3.5 w-3.5" />
                      <span>Attendees</span>
                    </Link>

                    <button
                      type="button"
                      disabled={exportingEventId === ev.id}
                      onClick={() => handleExportCsv(ev.id)}
                      className="inline-flex cursor-pointer items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      {exportingEventId === ev.id ? (
                        <Loader2 className="h-3.5 w-3.5 animate-spin" />
                      ) : (
                        <Download className="h-3.5 w-3.5" />
                      )}
                      <span>Export CSV</span>
                    </button>
                  </div>
                </div>

                <div className="space-y-1.5">
                  <div className="flex flex-wrap items-center justify-between gap-2 text-xs">
                    <span className="font-medium text-slate-700">
                      Occupancy: {ev.occupiedCount} / {ev.capacity} ({fillPercent}%)
                    </span>
                    <span className="font-medium text-emerald-700">
                      Checked in: {ev.checkedInCount} / {ev.occupiedCount} ({ev.checkInRatePercent}%)
                    </span>
                  </div>
                  <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                    <div
                      className="h-full rounded-full bg-slate-900 transition-all"
                      style={{ width: `${fillPercent}%` }}
                    />
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}