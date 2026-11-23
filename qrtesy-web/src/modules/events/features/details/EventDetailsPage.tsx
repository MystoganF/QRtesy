import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Calendar, Clock, MapPin, Users, AlertCircle } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { formatEventDate, formatTimeRange } from '@/shared/lib/dateTime';
import { RegisterForm } from '@/modules/registrations/features/register/RegisterForm';
import { eventsApi } from '../../api/eventsApi';

export function EventDetailsPage() {
  const { eventId } = useParams<{ eventId: string }>();

  const { data: event, isLoading, isError } = useQuery({
    queryKey: ['event-details', eventId],
    queryFn: () => eventsApi.getDetails(eventId!),
    enabled: Boolean(eventId),
  });

  if (isLoading) {
    return <div className="h-72 animate-pulse rounded-xl border border-slate-200 bg-white p-6" />;
  }

  if (isError || !event) {
    return (
      <div className="space-y-4">
        <Link
          to="/"
          className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to events
        </Link>
        <div className="flex items-center gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>Event not found or no longer available.</span>
        </div>
      </div>
    );
  }

  const isEnded = new Date(event.endsAt).getTime() < Date.now();
  const isRegistrationClosed = event.full || isEnded || event.status !== 'PUBLISHED';

  return (
    <div className="space-y-6">
      <Link
        to="/"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to events
      </Link>

      <div className="grid gap-6 lg:grid-cols-5">
        <div className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-xs lg:col-span-3">
          <div className="flex flex-wrap items-center gap-2">
            {isEnded ? (
              <span className="rounded-full border border-slate-300 bg-slate-100 px-3 py-0.5 text-xs font-semibold text-slate-700">
                Ended
              </span>
            ) : event.full ? (
              <span className="rounded-full border border-red-200 bg-red-50 px-3 py-0.5 text-xs font-semibold text-red-700">
                Full
              </span>
            ) : (
              <span className="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-0.5 text-xs font-semibold text-emerald-700">
                {event.remainingSpots} of {event.capacity} spots left
              </span>
            )}
          </div>

          <h1 className="text-2xl font-bold tracking-tight text-slate-900">{event.title}</h1>
          <p className="text-sm leading-relaxed text-slate-600">{event.description}</p>

          <div className="space-y-2.5 border-t border-slate-100 pt-4 text-sm text-slate-700">
            <div className="flex items-center gap-2.5">
              <Calendar className="h-4 w-4 shrink-0 text-slate-400" />
              <span>{formatEventDate(event.startsAt)}</span>
            </div>
            <div className="flex items-center gap-2.5">
              <Clock className="h-4 w-4 shrink-0 text-slate-400" />
              <span>{formatTimeRange(event.startsAt, event.endsAt)}</span>
            </div>
            <div className="flex items-center gap-2.5">
              <MapPin className="h-4 w-4 shrink-0 text-slate-400" />
              <span>{event.venue}</span>
            </div>
            <div className="flex items-center gap-2.5">
              <Users className="h-4 w-4 shrink-0 text-slate-400" />
              <span>
                {event.confirmedCount} of {event.capacity} spots filled
              </span>
            </div>
          </div>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-xs lg:col-span-2">
          <h2 className="text-base font-semibold text-slate-900">Register for this event</h2>
          <p className="mt-1 mb-4 text-xs text-slate-500">
            Free admission. You will receive an instant QR ticket pass upon registration.
          </p>
          <RegisterForm eventId={event.id} disabled={isRegistrationClosed} />
        </div>
      </div>
    </div>
  );
}