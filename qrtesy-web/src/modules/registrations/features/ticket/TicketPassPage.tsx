import { useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { ArrowLeft, Search } from 'lucide-react';
import { TicketPassCard } from './TicketPassCard';
import type { RegistrationTicket } from '../../types/registration.types';

export function TicketPassPage() {
  const { ticketCode = '' } = useParams<{ ticketCode: string }>();
  const location = useLocation();
  const initialTicket = (location.state as { ticket?: RegistrationTicket } | null)?.ticket ?? null;
  const [ticket, setTicket] = useState<RegistrationTicket | null>(initialTicket);

  if (ticket) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <Link
            to="/"
            className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900"
          >
            <ArrowLeft className="h-4 w-4" />
            Back to events
          </Link>
          <Link
            to="/ticket/lookup"
            className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900"
          >
            <Search className="h-4 w-4" />
            Look up another ticket
          </Link>
        </div>

        <TicketPassCard ticket={ticket} onTicketUpdated={setTicket} />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md space-y-4 rounded-xl border border-slate-200 bg-white p-6 text-center shadow-xs">
      <h1 className="text-lg font-semibold text-slate-900">Verify your email to view pass</h1>
      <p className="text-sm text-slate-600">
        For your privacy, please verify the email address used when registering ticket{' '}
        <span className="font-mono font-semibold text-slate-900">{ticketCode}</span>.
      </p>
      <Link
        to={`/ticket/lookup?code=${encodeURIComponent(ticketCode)}`}
        className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white hover:bg-slate-800"
      >
        <Search className="h-4 w-4" />
        Continue to Ticket Lookup
      </Link>
    </div>
  );
}