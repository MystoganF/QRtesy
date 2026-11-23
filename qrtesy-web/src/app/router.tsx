import { createBrowserRouter } from 'react-router-dom';
import { AppShell } from '@/shared/components/AppShell';

function Placeholder({ title }: { title: string }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-6">
      <h1 className="text-lg font-semibold text-slate-900">{title}</h1>
      <p className="mt-1 text-sm text-slate-500">Ready for slice implementation.</p>
    </div>
  );
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppShell />,
    children: [
      { index: true, element: <Placeholder title="Upcoming Events" /> },
      { path: 'events/:eventId', element: <Placeholder title="Event Details" /> },
      { path: 'ticket/lookup', element: <Placeholder title="Find Your Ticket" /> },
      { path: 'staff/login', element: <Placeholder title="Staff Sign In" /> },
      { path: 'staff/checkin', element: <Placeholder title="Door Check-in" /> },
      { path: 'staff/events/:eventId/attendees', element: <Placeholder title="Attendee List" /> },
    ],
  },
]);