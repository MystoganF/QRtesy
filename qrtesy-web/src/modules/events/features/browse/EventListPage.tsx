import { useQuery } from '@tanstack/react-query';
import { Calendar, Clock, MapPin, Users, ArrowRight, AlertCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import { formatEventDate, formatTimeRange } from '@/shared/lib/dateTime';
import { eventsApi } from '../../api/eventsApi';

export function EventListPage() {
  const { data: events, isLoading, isError } = useQuery({
    queryKey: ['public-events'],
    queryFn: eventsApi.listPublic,
  });

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-48 animate-pulse rounded-lg bg-slate-200" />
        <div className="grid gap-4 sm:grid-cols-2">
          {[1, 2].map((i) => (
            <div key={i} className="h-52 animate-pulse rounded-xl border border-slate-200 bg-white p-6" />
          ))}
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex items-center gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
        <AlertCircle className="h-5 w-5 shrink-0" />
        <span>Unable to load upcoming events. Please refresh and try again.</span>
      </div>
    );
  }

  const now = Date.now();
  const visibleEvents = (events ?? []).filter((ev) => new Date(ev.endsAt).getTime() >= now);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Upcoming Events</h1>
        <p className="mt-1 text-sm text-slate-600">
          Select an event below to view details and claim your free QR ticket pass.
        </p>
      </div>

      {visibleEvents.length === 0 ? (
        <div className="rounded-xl border border-slate-200 bg-white p-10 text-center">
          <p className="text-sm font-medium text-slate-900">No upcoming events right now</p>
          <p className="mt-1 text-sm text-slate-500">Check back later for newly published sessions.</p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {visibleEvents.map((event) => (
            <Link
              key={event.id}
              to={`/events/${event.id}`}
              className="group flex flex-col justify-between rounded-xl border border-slate-200 bg-white p-6 shadow-xs transition hover:border-slate-300 hover:shadow-md"
            >
              <div>
                <div className="flex items-start justify-between gap-3">
                  <h2 className="text-lg font-semibold text-slate-900 group-hover:text-slate-700">
                    {event.title}
                  </h2>
                  {event.full ? (
                    <span className="shrink-0 rounded-full border border-red-200 bg-red-50 px-2.5 py-0.5 text-xs font-semibold text-red-700">
                      Full
                    </span>
                  ) : (
                    <span className="shrink-0 rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-700">
                      {event.remainingSpots} of {event.capacity} spots left
                    </span>
                  )}
                </div>

                <div className="mt-4 space-y-2 text-sm text-slate-600">
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4 shrink-0 text-slate-400" />
                    <span>{formatEventDate(event.startsAt)}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4 shrink-0 text-slate-400" />
                    <span>{formatTimeRange(event.startsAt, event.endsAt)}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <MapPin className="h-4 w-4 shrink-0 text-slate-400" />
                    <span>{event.venue}</span>
                  </div>
                </div>
              </div>

              <div className="mt-6 flex items-center justify-between border-t border-slate-100 pt-4 text-sm font-medium text-slate-900">
                <span className="inline-flex items-center gap-1.5 text-xs text-slate-500">
                  <Users className="h-3.5 w-3.5" />
                  {event.confirmedCount} / {event.capacity} registered
                </span>
                <span className="inline-flex items-center gap-1 transition-transform group-hover:translate-x-0.5">
                  View details
                  <ArrowRight className="h-4 w-4" />
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}