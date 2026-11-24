import { useQuery } from '@tanstack/react-query';
import { CheckCircle2, Users } from 'lucide-react';
import { attendeesApi } from '@/modules/registrations/features/attendees/attendeesApi';

interface LiveCheckInCounterProps {
  eventId: string;
}

export function LiveCheckInCounter({ eventId }: LiveCheckInCounterProps) {
  const { data, isLoading } = useQuery({
    queryKey: ['event-attendees', eventId],
    queryFn: () => attendeesApi.listByEvent(eventId),
    enabled: Boolean(eventId),
    refetchInterval: 5000,
  });

  if (isLoading || !data) {
    return (
      <div className="h-20 animate-pulse rounded-xl border border-slate-200 bg-slate-100" />
    );
  }

  const checkedIn = data.checkedInCount;
  const expected = data.totalOccupied;
  const percentage = expected > 0 ? Math.min(100, Math.round((checkedIn / expected) * 100)) : 0;

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-50 text-emerald-700">
            <CheckCircle2 className="h-4 w-4" />
          </div>
          <div>
            <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
              Live Gate Attendance
            </p>
            <p className="text-lg font-bold text-slate-900">
              {checkedIn} <span className="text-sm font-normal text-slate-500">/ {expected} checked in</span>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-1.5 rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">
          <Users className="h-3.5 w-3.5 text-slate-500" />
          <span>
            {percentage}% · Capacity {data.capacity}
          </span>
        </div>
      </div>

      <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-slate-100">
        <div
          className="h-full rounded-full bg-emerald-600 transition-all duration-300"
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
}