import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppShell } from '@/shared/components/AppShell';
import { EventListPage } from '@/modules/events/features/browse/EventListPage';
import { EventDetailsPage } from '@/modules/events/features/details/EventDetailsPage';
import { StaffDashboardPage } from '@/modules/events/features/dashboard/StaffDashboardPage';
import { StaffEventsPage } from '@/modules/events/features/manage/StaffEventsPage';
import { TicketLookupPage } from '@/modules/registrations/features/ticket/TicketLookupPage';
import { TicketPassPage } from '@/modules/registrations/features/ticket/TicketPassPage';
import { StaffLoginPage } from '@/modules/auth/features/login/StaffLoginPage';
import { RequireStaffAuth } from '@/modules/auth/components/RequireStaffAuth';
import { DoorCheckInPage } from '@/modules/checkin/features/scan/DoorCheckInPage';
import { StaffAttendeesPage } from '@/modules/registrations/features/attendees/StaffAttendeesPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppShell />,
    children: [
      { index: true, element: <EventListPage /> },
      { path: 'events/:eventId', element: <EventDetailsPage /> },
      { path: 'ticket/lookup', element: <TicketLookupPage /> },
      { path: 'ticket/:ticketCode', element: <TicketPassPage /> },
      { path: 'staff/login', element: <StaffLoginPage /> },
      {
        path: 'staff',
        element: <RequireStaffAuth />,
        children: [
          { index: true, element: <Navigate to="/staff/dashboard" replace /> },
          { path: 'dashboard', element: <StaffDashboardPage /> },
          { path: 'events', element: <StaffEventsPage /> },
          { path: 'checkin', element: <DoorCheckInPage /> },
          { path: 'attendees', element: <StaffAttendeesPage /> },
          { path: 'events/:eventId/attendees', element: <StaffAttendeesPage /> },
        ],
      },
    ],
  },
]);