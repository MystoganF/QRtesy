import { QRCodeSVG } from 'qrcode.react';
import { Calendar, CheckCircle2, Clock, MapPin, User } from 'lucide-react';
import { formatDateTime, formatEventDate, formatTimeRange } from '@/shared/lib/dateTime';
import type { RegistrationTicket } from '../../types/registration.types';

interface TicketPassCardProps {
  ticket: RegistrationTicket;
}

export function TicketPassCard({ ticket }: TicketPassCardProps) {
  const isCheckedIn = ticket.status === 'CHECKED_IN';

  return (
    <div className="mx-auto max-w-md overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
      <div className="bg-slate-900 px-6 py-5 text-white">
        <div className="flex items-center justify-between gap-2">
          <span className="text-xs font-medium uppercase tracking-wider text-slate-300">
            Official Entry Pass
          </span>
          <span
            className={
              isCheckedIn
                ? 'rounded-full bg-emerald-500/20 px-2.5 py-0.5 text-xs font-semibold text-emerald-300'
                : 'rounded-full bg-white/15 px-2.5 py-0.5 text-xs font-semibold text-white'
            }
          >
            {isCheckedIn ? 'Checked In' : 'Confirmed'}
          </span>
        </div>
        <h1 className="mt-2 text-xl font-bold leading-snug">{ticket.eventTitle}</h1>
      </div>

      <div className="flex flex-col items-center border-b border-dashed border-slate-200 px-6 py-6">
        <div className="rounded-xl border border-slate-200 bg-white p-4">
          <QRCodeSVG value={ticket.ticketCode} size={192} level="M" />
        </div>
        <p className="mt-3 font-mono text-lg font-bold tracking-widest text-slate-900">
          {ticket.ticketCode}
        </p>
        <p className="mt-1 text-center text-xs text-slate-500">
          Present this QR code or ticket code at the event entrance.
        </p>
      </div>

      <div className="space-y-3 px-6 py-5 text-sm">
        <div className="flex items-start gap-2.5">
          <User className="mt-0.5 h-4 w-4 shrink-0 text-slate-400" />
          <div>
            <p className="text-xs text-slate-500">Attendee</p>
            <p className="font-semibold text-slate-900">{ticket.fullName}</p>
            <p className="text-xs text-slate-500">{ticket.email}</p>
          </div>
        </div>

        <div className="flex items-start gap-2.5">
          <Calendar className="mt-0.5 h-4 w-4 shrink-0 text-slate-400" />
          <div>
            <p className="text-xs text-slate-500">Date</p>
            <p className="font-medium text-slate-900">{formatEventDate(ticket.eventStartsAt)}</p>
          </div>
        </div>

        <div className="flex items-start gap-2.5">
          <Clock className="mt-0.5 h-4 w-4 shrink-0 text-slate-400" />
          <div>
            <p className="text-xs text-slate-500">Time</p>
            <p className="font-medium text-slate-900">
              {formatTimeRange(ticket.eventStartsAt, ticket.eventEndsAt)}
            </p>
          </div>
        </div>

        <div className="flex items-start gap-2.5">
          <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-slate-400" />
          <div>
            <p className="text-xs text-slate-500">Venue</p>
            <p className="font-medium text-slate-900">{ticket.eventVenue}</p>
          </div>
        </div>

        {isCheckedIn && ticket.checkedInAt && (
          <div className="mt-2 flex items-center gap-2 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs font-medium text-emerald-800">
            <CheckCircle2 className="h-4 w-4 shrink-0" />
            <span>Checked in on {formatDateTime(ticket.checkedInAt)}</span>
          </div>
        )}
      </div>
    </div>
  );
}