import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  CalendarCog,
  ClipboardList,
  LayoutDashboard,
  Loader2,
  LogOut,
  QrCode,
  UserCheck,
} from 'lucide-react';
import { Navigate, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { cn } from '@/shared/lib/cn';
import { authApi } from '../api/authApi';

export function RequireStaffAuth() {
  const location = useLocation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: staff, isLoading, isError } = useQuery({
    queryKey: ['staff-me'],
    queryFn: authApi.me,
    retry: false,
  });

  const logoutMutation = useMutation({
    mutationFn: authApi.logout,
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: ['staff-me'] });
      navigate('/staff/login', { replace: true });
    },
  });

  if (isLoading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center text-sm text-slate-500">
        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
        <span>Verifying staff session...</span>
      </div>
    );
  }

  if (isError || !staff) {
    return <Navigate to="/staff/login" state={{ from: location }} replace />;
  }

  const isOrganizer = staff.role === 'ORGANIZER';

  if (!isOrganizer && location.pathname.startsWith('/staff/events') && !location.pathname.endsWith('/attendees')) {
    return <Navigate to="/staff/checkin" replace />;
  }

  const tabClass = ({ isActive }: { isActive: boolean }) =>
    cn(
      'inline-flex cursor-pointer items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium transition-colors',
      isActive
        ? 'bg-slate-900 text-white'
        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
    );

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-xs sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-wrap items-center gap-1.5">
          <NavLink to="/staff/dashboard" className={tabClass}>
            <LayoutDashboard className="h-4 w-4" />
            <span>Dashboard</span>
          </NavLink>
          {isOrganizer && (
            <NavLink to="/staff/events" className={tabClass}>
              <CalendarCog className="h-4 w-4" />
              <span>Events</span>
            </NavLink>
          )}
          <NavLink to="/staff/checkin" className={tabClass}>
            <QrCode className="h-4 w-4" />
            <span>Door Check-in</span>
          </NavLink>
          <NavLink to="/staff/attendees" className={tabClass}>
            <ClipboardList className="h-4 w-4" />
            <span>Attendee List</span>
          </NavLink>
        </div>

        <div className="flex items-center justify-between gap-3 border-t border-slate-100 pt-3 sm:border-t-0 sm:pt-0">
          <span className="inline-flex items-center gap-1.5 text-xs font-medium text-slate-600">
            <UserCheck className="h-3.5 w-3.5 text-emerald-600" />
            <span>{staff.name}</span>
            <span className="rounded-full bg-slate-100 px-2 py-0.5 text-[11px] font-semibold text-slate-700">
              {isOrganizer ? 'Organizer' : 'Door Staff'}
            </span>
          </span>
          <button
            type="button"
            onClick={() => logoutMutation.mutate()}
            disabled={logoutMutation.isPending}
            className="inline-flex cursor-pointer items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-700 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <LogOut className="h-3.5 w-3.5" />
            <span>Sign out</span>
          </button>
        </div>
      </div>

      <Outlet />
    </div>
  );
}