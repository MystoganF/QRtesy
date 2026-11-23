import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { AlertCircle, CheckCircle2, Clock, Hourglass, Search, Users, XCircle } from 'lucide-react';
import { eventsApi } from '@/modules/events/api/eventsApi';
import { registrationsApi } from '@/modules/registrations/api/registrationsApi';
import { formatDateTime, formatEventDate } from '@/shared/lib/dateTime';
import { ApiError } from '@/shared/types/api';
import { attendeesApi } from './attendeesApi';

export function StaffAttendeesPage() {
  const { eventId: routeEventId } = useParams<{ eventId?: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [selectedEventId, setSelectedEventId] = useState<string>(routeEventId ?? '');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [actionError, setActionError] = useState<string | null>(null);
  const [confirmCancelId, setConfirmCancelId] = useState<string | null>(null);

  const { data: events } = useQuery({
    queryKey: ['public-events'],
    queryFn: eventsApi.listPublic,
  });

  useEffect(() => {
    if (routeEventId) {
      setSelectedEventId(routeEventId);
    } else if (!selectedEventId && events && events.length > 0) {
      setSelectedEventId(events[0].id);
    }
  }, [routeEventId, events, selectedEventId]);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['event-attendees', selectedEventId, searchQuery],
    queryFn: () => attendeesApi.listByEvent(selectedEventId, searchQuery),
    enabled: Boolean(selectedEventId),
  });

  const cancelMutation = useMutation({
    mutationFn: (registrationId: string) => registrationsApi.cancelByStaff(registrationId),
    onSuccess: () => {
      setConfirmCancelId(null);
      setActionError(null);
      queryClient.invalidateQueries({ queryKey: ['event-attendees', selectedEventId] });
      queryClient.invalidateQueries({ queryKey: ['staff-events'] });
      queryClient.invalidateQueries({ queryKey: ['public-events'] });
    },
    onError: (err) => {
      if (err instanceof ApiError) {
        setActionError(err.message);
      } else {
        setActionError('Unable to cancel registration.');
      }
    },
  });

  const handleEventChange = (nextId: string) => {
    setSelectedEventId(nextId);
    setSearchQuery('');
    setConfirmCancelId(null);
    setActionError(null);
    navigate(`/staff/events/${nextId}/attendees`, { replace: true });
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 rounded-xl border border-slate-200 bg-white p-6 shadow-xs sm:flex-row sm:items-end sm:justify-between">
        <div className="w-full sm:max-w-sm">
          <label htmlFor="attendeeEventSelect" className="block text-sm font-medium text-slate-700">
            Select Event
          </label>
          <select
            id="attendeeEventSelect"
            value={selectedEventId}
            onChange={(e) => handleEventChange(e.target.value)}
            className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
          >
            {(events ?? []).map((ev) => (
              <option key={ev.id} value={ev.id}>
                {ev.title} ({formatEventDate(ev.startsAt)})
              </option>
            ))}
          </select>
        </div>

        <div className="relative w-full sm:max-w-xs">
          <label htmlFor="attendeeSearch" className="block text-sm font-medium text-slate-700">
            Search Attendees
          </label>
          <div className="relative mt-1">
            <Search className="pointer-events-none absolute top-2.5 left-3 h-4 w-4 text-slate-400" />
            <input
              id="attendeeSearch"
              type="search"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Name or ticket code..."
              className="block w-full rounded-lg border border-slate-300 bg-white py-2 pr-3 pl-9 text-sm text-slate-900 placeholder:text-slate-400 focus:border-slate-900 focus:outline-none"
            />
          </div>
        </div>
      </div>

      {actionError && (
        <div
          role="alert"
          className="flex items-center gap-2.5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800"
        >
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>{actionError}</span>
        </div>
      )}

      {data && (
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="flex items-center justify-between rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
            <div>
              <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
                Registered / Capacity
              </p>
              <p className="mt-1 text-2xl font-bold text-slate-900">
                {data.totalOccupied} / {data.capacity}
              </p>
            </div>
            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-100 text-slate-700">
              <Users className="h-5 w-5" />
            </span>
          </div>

          <div className="flex items-center justify-between rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
            <div>
              <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
                Checked In / Registered
              </p>
              <p className="mt-1 text-2xl font-bold text-emerald-700">
                {data.checkedInCount} / {data.totalOccupied}
              </p>
            </div>
            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-emerald-50 text-emerald-700">
              <CheckCircle2 className="h-5 w-5" />
            </span>
          </div>

          <div className="flex items-center justify-between rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
            <div>
              <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
                Waitlisted
              </p>
              <p className="mt-1 text-2xl font-bold text-amber-700">
                {data.waitlistedCount}
              </p>
            </div>
            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-amber-50 text-amber-700">
              <Hourglass className="h-5 w-5" />
            </span>
          </div>
        </div>
      )}

      {isError && (
        <div className="flex items-center gap-2.5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>Unable to load attendees for this event.</span>
        </div>
      )}

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xs">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
            <thead className="bg-slate-50 text-xs font-semibold uppercase tracking-wider text-slate-500">
              <tr>
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Mobile</th>
                <th className="px-4 py-3">Ticket Code</th>
                <th className="px-4 py-3">Registered At</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 bg-white">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="px-4 py-8 text-center text-slate-500">
                    Loading attendees...
                  </td>
                </tr>
              ) : !data || data.attendees.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-4 py-8 text-center text-slate-500">
                    No matching registrations found.
                  </td>
                </tr>
              ) : (
                data.attendees.map((row) => {
                  const canCancel = row.status === 'CONFIRMED' || row.status === 'WAITLISTED';
                  const isConfirming = confirmCancelId === row.registrationId;

                  return (
                    <tr key={row.registrationId} className="hover:bg-slate-50/80">
                      <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-900">
                        {row.fullName}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">{row.email}</td>
                      <td className="whitespace-nowrap px-4 py-3 font-mono text-xs text-slate-600">
                        {row.mobile}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 font-mono text-xs font-semibold text-slate-900">
                        {row.ticketCode}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-xs text-slate-600">
                        {formatDateTime(row.registeredAt)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3">
                        {row.status === 'CHECKED_IN' ? (
                          <span className="inline-flex items-center gap-1 rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-700">
                            <CheckCircle2 className="h-3.5 w-3.5" />
                            <span>
                              Checked in
                              {row.checkedInAt ? ` · ${formatDateTime(row.checkedInAt)}` : ''}
                            </span>
                          </span>
                        ) : row.status === 'WAITLISTED' ? (
                          <span className="inline-flex items-center gap-1 rounded-full border border-amber-200 bg-amber-50 px-2.5 py-0.5 text-xs font-semibold text-amber-800">
                            <Hourglass className="h-3.5 w-3.5" />
                            <span>
                              Waitlisted{row.waitlistPosition ? ` (#${row.waitlistPosition})` : ''}
                            </span>
                          </span>
                        ) : row.status === 'CANCELLED' ? (
                          <span className="inline-flex items-center gap-1 rounded-full border border-red-200 bg-red-50 px-2.5 py-0.5 text-xs font-semibold text-red-700">
                            <XCircle className="h-3.5 w-3.5" />
                            <span>Cancelled</span>
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-700">
                            <Clock className="h-3.5 w-3.5" />
                            <span>Confirmed</span>
                          </span>
                        )}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-right">
                        {canCancel ? (
                          isConfirming ? (
                            <div className="inline-flex items-center gap-1.5">
                              <button
                                type="button"
                                disabled={cancelMutation.isPending}
                                onClick={() => cancelMutation.mutate(row.registrationId)}
                                className="rounded-md bg-red-600 px-2.5 py-1 text-xs font-semibold text-white hover:bg-red-700 disabled:opacity-50"
                              >
                                Confirm
                              </button>
                              <button
                                type="button"
                                disabled={cancelMutation.isPending}
                                onClick={() => setConfirmCancelId(null)}
                                className="rounded-md border border-slate-200 px-2 py-1 text-xs font-medium text-slate-600 hover:bg-slate-100"
                              >
                                Keep
                              </button>
                            </div>
                          ) : (
                            <button
                              type="button"
                              onClick={() => setConfirmCancelId(row.registrationId)}
                              className="rounded-md border border-red-200 px-2.5 py-1 text-xs font-semibold text-red-700 hover:bg-red-50"
                            >
                              Cancel
                            </button>
                          )
                        ) : (
                          <span className="text-xs text-slate-400">—</span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}