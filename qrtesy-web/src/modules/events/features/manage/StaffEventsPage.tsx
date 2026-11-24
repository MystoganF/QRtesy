import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  AlertCircle,
  Calendar,
  Clock,
  Lock,
  MapPin,
  Pencil,
  Plus,
  Users,
} from 'lucide-react';
import { formatEventDate, formatTimeRange } from '@/shared/lib/dateTime';
import { eventsApi } from '../../api/eventsApi';
import type { EventStatus, StaffEventItem } from '../../types/event.types';
import { EventFormModal } from './EventFormModal';

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

export function StaffEventsPage() {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<StaffEventItem | null>(null);

  const { data: events, isLoading, isError } = useQuery({
    queryKey: ['staff-events'],
    queryFn: eventsApi.listStaffAll,
  });

  const closeMutation = useMutation({
    mutationFn: (eventId: string) => eventsApi.closeEvent(eventId),
    onSuccess: (_data, eventId) => {
      queryClient.invalidateQueries({ queryKey: ['staff-events'] });
      queryClient.invalidateQueries({ queryKey: ['staff-dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['staff-audit-logs'] });
      queryClient.invalidateQueries({ queryKey: ['public-events'] });
      queryClient.invalidateQueries({ queryKey: ['event-details', eventId] });
    },
  });

  const openCreateModal = () => {
    setEditingEvent(null);
    setModalOpen(true);
  };

  const openEditModal = (event: StaffEventItem) => {
    setEditingEvent(event);
    setModalOpen(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-6 shadow-xs sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Manage Events</h1>
          <p className="mt-1 text-sm text-slate-600">
            Create new events, publish drafts, adjust capacities, or close registration.
          </p>
        </div>

        <button
          type="button"
          onClick={openCreateModal}
          className="inline-flex items-center justify-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800"
        >
          <Plus className="h-4 w-4" />
          <span>New Event</span>
        </button>
      </div>

      {isError && (
        <div className="flex items-center gap-2.5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>Unable to load events.</span>
        </div>
      )}

      {isLoading ? (
        <div className="space-y-3">
          {[1, 2].map((i) => (
            <div key={i} className="h-32 animate-pulse rounded-xl border border-slate-200 bg-white p-6" />
          ))}
        </div>
      ) : (
        <div className="space-y-4">
          {(events ?? []).map((event) => (
            <div
              key={event.id}
              className="flex flex-col justify-between gap-4 rounded-xl border border-slate-200 bg-white p-5 shadow-xs sm:flex-row sm:items-center"
            >
              <div className="space-y-2">
                <div className="flex flex-wrap items-center gap-2.5">
                  <h2 className="text-base font-bold text-slate-900">{event.title}</h2>
                  <StatusBadge status={event.status} />
                </div>

                <div className="flex flex-wrap gap-x-5 gap-y-1 text-xs text-slate-600">
                  <span className="inline-flex items-center gap-1.5">
                    <Calendar className="h-3.5 w-3.5 text-slate-400" />
                    {formatEventDate(event.startsAt)}
                  </span>
                  <span className="inline-flex items-center gap-1.5">
                    <Clock className="h-3.5 w-3.5 text-slate-400" />
                    {formatTimeRange(event.startsAt, event.endsAt)}
                  </span>
                  <span className="inline-flex items-center gap-1.5">
                    <MapPin className="h-3.5 w-3.5 text-slate-400" />
                    {event.venue}
                  </span>
                  <span className="inline-flex items-center gap-1.5 font-medium text-slate-800">
                    <Users className="h-3.5 w-3.5 text-slate-400" />
                    <span>
                      {event.occupiedCount} / {event.capacity} registered
                    </span>
                    {event.waitlistCount > 0 && (
                      <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[11px] font-semibold text-amber-800">
                        +{event.waitlistCount} waitlisted
                      </span>
                    )}
                  </span>
                </div>
              </div>

              <div className="flex shrink-0 items-center gap-2">
                {event.status !== 'CLOSED' && (
                  <>
                    <button
                      type="button"
                      onClick={() => openEditModal(event)}
                      className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-100"
                    >
                      <Pencil className="h-3.5 w-3.5" />
                      <span>Edit</span>
                    </button>

                    <button
                      type="button"
                      disabled={closeMutation.isPending}
                      onClick={() => closeMutation.mutate(event.id)}
                      className="inline-flex items-center gap-1.5 rounded-lg border border-red-200 bg-red-50 px-3 py-1.5 text-xs font-semibold text-red-700 transition hover:bg-red-100 disabled:opacity-50"
                    >
                      <Lock className="h-3.5 w-3.5" />
                      <span>Close</span>
                    </button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {modalOpen && (
        <EventFormModal
          editingEvent={editingEvent}
          onClose={() => setModalOpen(false)}
        />
      )}
    </div>
  );
}