import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AlertTriangle, CheckCircle2, Loader2, QrCode, XCircle } from 'lucide-react';
import { eventsApi } from '@/modules/events/api/eventsApi';
import { LiveCheckInCounter } from '@/modules/checkin/components/LiveCheckInCounter';
import { QrCameraScanner } from '@/modules/checkin/components/QrCameraScanner';
import {
  ScanFeedbackToast,
  type ScanToastItem,
} from '@/modules/checkin/components/ScanFeedbackToast';
import { formatDateTime, formatEventDate } from '@/shared/lib/dateTime';
import { ApiError } from '@/shared/types/api';
import { checkinApi } from './checkinApi';
import type { ScanTicketResponse } from './checkin.types';

function normalizeTicketInput(raw: string): string {
  if (/^qrt1\./i.test(raw)) {
    const parts = raw.split('.');
    if (parts.length >= 2) {
      parts[0] = 'QRT1';
      parts[1] = parts[1].toUpperCase();
      return parts.join('.');
    }
  }
  return raw.toUpperCase();
}

export function DoorCheckInPage() {
  const queryClient = useQueryClient();
  const inputRef = useRef<HTMLInputElement>(null);

  const [selectedEventId, setSelectedEventId] = useState<string>('');
  const [ticketCode, setTicketCode] = useState<string>('');
  const [scanResult, setScanResult] = useState<ScanTicketResponse | null>(null);
  const [requestError, setRequestError] = useState<string | null>(null);
  const [activeToast, setActiveToast] = useState<ScanToastItem | null>(null);

  const dismissToast = useCallback(() => setActiveToast(null), []);

  const { data: events, isLoading: loadingEvents } = useQuery({
    queryKey: ['public-events'],
    queryFn: eventsApi.listPublic,
  });

  useEffect(() => {
    if (!selectedEventId && events && events.length > 0) {
      setSelectedEventId(events[0].id);
    }
  }, [events, selectedEventId]);

  const scanMutation = useMutation({
    mutationFn: (codeToScan: string) => checkinApi.scanTicket(selectedEventId, codeToScan),
    onSuccess: (data) => {
      setRequestError(null);
      setScanResult(data);
      setActiveToast({ id: Date.now(), kind: 'scan', result: data });
      setTicketCode('');
      if (data.outcome === 'CHECKED_IN') {
        queryClient.invalidateQueries({ queryKey: ['event-attendees', selectedEventId] });
        queryClient.invalidateQueries({ queryKey: ['staff-events'] });
      }
      inputRef.current?.focus();
    },
    onError: (err) => {
      setScanResult(null);
      const message =
        err instanceof ApiError ? err.message : 'Unable to verify ticket right now.';
      setRequestError(message);
      setActiveToast({ id: Date.now(), kind: 'error', message });
      inputRef.current?.focus();
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    const trimmed = ticketCode.trim();
    if (!selectedEventId || !trimmed || scanMutation.isPending) return;
    scanMutation.mutate(trimmed);
  };

  const handleCameraScan = (decodedText: string) => {
    const trimmed = decodedText.trim();
    if (!selectedEventId || !trimmed || scanMutation.isPending) return;
    scanMutation.mutate(trimmed);
  };

  return (
    <div className="mx-auto max-w-xl space-y-6">
      <ScanFeedbackToast toast={activeToast} onDismiss={dismissToast} />

      {selectedEventId && <LiveCheckInCounter eventId={selectedEventId} />}

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
        <h1 className="text-xl font-bold text-slate-900">Door Check-in</h1>
        <p className="mt-1 text-sm text-slate-600">
          Select the active event, then scan the QR pass or enter the ticket code manually.
        </p>

        <div className="mt-5 space-y-4">
          <div>
            <label htmlFor="checkinEvent" className="block text-sm font-medium text-slate-700">
              Event
            </label>
            <select
              id="checkinEvent"
              value={selectedEventId}
              disabled={loadingEvents}
              onChange={(e) => {
                setSelectedEventId(e.target.value);
                setScanResult(null);
                setRequestError(null);
                setActiveToast(null);
                inputRef.current?.focus();
              }}
              className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
            >
              {(events ?? []).map((ev) => (
                <option key={ev.id} value={ev.id}>
                  {ev.title} ({formatEventDate(ev.startsAt)})
                </option>
              ))}
            </select>
          </div>

          <QrCameraScanner
            onScan={handleCameraScan}
            disabled={!selectedEventId || scanMutation.isPending}
          />

          <form onSubmit={handleSubmit}>
            <label htmlFor="scanTicketCode" className="block text-sm font-medium text-slate-700">
              Manual Ticket Code
            </label>
            <div className="mt-1 flex gap-2">
              <input
                ref={inputRef}
                id="scanTicketCode"
                type="text"
                autoFocus
                autoComplete="off"
                value={ticketCode}
                onChange={(e) => setTicketCode(normalizeTicketInput(e.target.value))}
                placeholder="Enter code (e.g., QRT-ZTTZZ5)"
                className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 font-mono text-base tracking-wider text-slate-900 placeholder:font-sans placeholder:text-sm placeholder:tracking-normal placeholder:text-slate-400 focus:border-slate-900 focus:outline-none"
              />
              <button
                type="submit"
                disabled={!selectedEventId || !ticketCode.trim() || scanMutation.isPending}
                className="inline-flex shrink-0 items-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:bg-slate-300"
              >
                {scanMutation.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <QrCode className="h-4 w-4" />
                )}
                <span>Check In</span>
              </button>
            </div>
          </form>
        </div>
      </div>

      {requestError && (
        <div
          role="alert"
          className="flex items-start gap-3 rounded-xl border-2 border-red-300 bg-red-50 p-5 text-red-950"
        >
          <XCircle className="mt-0.5 h-6 w-6 shrink-0 text-red-600" />
          <div>
            <p className="text-base font-bold">Check-in Blocked</p>
            <p className="mt-0.5 text-sm text-red-800">{requestError}</p>
          </div>
        </div>
      )}

      {scanResult && scanResult.outcome === 'CHECKED_IN' && (
        <div
          role="status"
          className="flex items-start gap-3 rounded-xl border-2 border-emerald-400 bg-emerald-50 p-5 text-emerald-950"
        >
          <CheckCircle2 className="mt-0.5 h-7 w-7 shrink-0 text-emerald-600" />
          <div className="space-y-1">
            <span className="inline-block rounded-md bg-emerald-600 px-2 py-0.5 text-xs font-bold uppercase tracking-wide text-white">
              Checked in
            </span>
            <p className="text-xl font-bold text-emerald-950">{scanResult.fullName}</p>
            <p className="font-mono text-xs text-emerald-800">Ticket: {scanResult.ticketCode}</p>
          </div>
        </div>
      )}

      {scanResult && scanResult.outcome === 'ALREADY_CHECKED_IN' && (
        <div
          role="status"
          className="flex items-start gap-3 rounded-xl border-2 border-amber-400 bg-amber-50 p-5 text-amber-950"
        >
          <AlertTriangle className="mt-0.5 h-7 w-7 shrink-0 text-amber-600" />
          <div className="space-y-1">
            <span className="inline-block rounded-md bg-amber-500 px-2 py-0.5 text-xs font-bold uppercase tracking-wide text-amber-950">
              Already checked in
            </span>
            <p className="text-xl font-bold text-amber-950">{scanResult.fullName}</p>
            {scanResult.checkedInAt && (
              <p className="text-sm font-medium text-amber-900">
                First checked in: {formatDateTime(scanResult.checkedInAt)}
              </p>
            )}
            <p className="font-mono text-xs text-amber-800">Ticket: {scanResult.ticketCode}</p>
          </div>
        </div>
      )}

      {scanResult &&
        (scanResult.outcome === 'INVALID_TICKET' || scanResult.outcome === 'NOT_CONFIRMED') && (
          <div
            role="alert"
            className="flex items-start gap-3 rounded-xl border-2 border-red-400 bg-red-50 p-5 text-red-950"
          >
            <XCircle className="mt-0.5 h-7 w-7 shrink-0 text-red-600" />
            <div className="space-y-1">
              <span className="inline-block rounded-md bg-red-600 px-2 py-0.5 text-xs font-bold uppercase tracking-wide text-white">
                {scanResult.outcome === 'INVALID_TICKET' ? 'Invalid ticket' : scanResult.message}
              </span>
              <p className="text-base font-bold text-red-950">{scanResult.message}</p>
              <p className="font-mono text-xs text-red-800">Code scanned: {scanResult.ticketCode}</p>
            </div>
          </div>
        )}
    </div>
  );
}