import { createBrowserRouter } from 'react-router-dom';
import { AppShell } from '@/shared/components/AppShell';
import { EventListPage } from '@/modules/events/features/browse/EventListPage';
import { EventDetailsPage } from '@/modules/events/features/details/EventDetailsPage';
import { TicketPassPage } from '@/modules/registrations/features/ticket/TicketPassPage';
import { TicketLookupPage } from '@/modules/registrations/features/ticket/TicketLookupPage';

function StaffPlaceholder({ title }: { title: string }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-6">
      <h1 className="text-lg font-semibold text-slate-900">{title}</h1>
      <p className="mt-1 text-sm text-slate-500">Ready for staff slice wiring.</p>
    </div>
  );
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppShell />,
    children: [
      { index: true, element: <EventListPage /> },
      { path: 'events/:eventId', element: <EventDetailsPage /> },
      { path: 'ticket/lookup', element: <TicketLookupPage /> },
      { path: 'ticket/:ticketCode', element: <TicketPassPage /> },
      { path: 'staff/login', element: <StaffPlaceholder title="Staff Sign In" /> },
      { path: 'staff/checkin', element: <StaffPlaceholder title="Door Check-in" /> },
      { path: 'staff/events/:eventId/attendees', element: <StaffPlaceholder title="Attendee List" /> },
    ],
  },
]);