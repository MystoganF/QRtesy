import { useEffect } from 'react';
import { AlertTriangle, CheckCircle2, X, XCircle } from 'lucide-react';
import { formatDateTime } from '@/shared/lib/dateTime';
import type { ScanTicketResponse } from '../features/scan/checkin.types';

export type ScanToastItem =
  | { id: number; kind: 'scan'; result: ScanTicketResponse }
  | { id: number; kind: 'error'; message: string };

interface ScanFeedbackToastProps {
  toast: ScanToastItem | null;
  onDismiss: () => void;
  durationMs?: number;
}

export function ScanFeedbackToast({
  toast,
  onDismiss,
  durationMs = 3500,
}: ScanFeedbackToastProps) {
  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(onDismiss, durationMs);
    return () => window.clearTimeout(timer);
  }, [toast, durationMs, onDismiss]);

  if (!toast) return null;

  if (toast.kind === 'error') {
    return (
      <div className="fixed top-4 left-1/2 z-50 w-full max-w-md -translate-x-1/2 px-4">
        <div
          role="alert"
          className="flex items-start justify-between gap-3 rounded-xl border-2 border-red-400 bg-red-50 p-4 text-red-950 shadow-lg"
        >
          <div className="flex items-start gap-3">
            <XCircle className="mt-0.5 h-6 w-6 shrink-0 text-red-600" />
            <div className="space-y-0.5">
              <span className="inline-block rounded-md bg-red-600 px-2 py-0.5 text-[11px] font-bold uppercase tracking-wide text-white">
                Check-in Blocked
              </span>
              <p className="text-sm font-semibold text-red-950">{toast.message}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onDismiss}
            aria-label="Dismiss notification"
            className="rounded-lg p-1 text-red-700 transition hover:bg-red-100"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      </div>
    );
  }

  const { result } = toast;

  if (result.outcome === 'CHECKED_IN') {
    return (
      <div className="fixed top-4 left-1/2 z-50 w-full max-w-md -translate-x-1/2 px-4">
        <div
          role="status"
          className="flex items-start justify-between gap-3 rounded-xl border-2 border-emerald-400 bg-emerald-50 p-4 text-emerald-950 shadow-lg"
        >
          <div className="flex items-start gap-3">
            <CheckCircle2 className="mt-0.5 h-6 w-6 shrink-0 text-emerald-600" />
            <div className="space-y-0.5">
              <div className="flex items-center gap-2">
                <span className="inline-block rounded-md bg-emerald-600 px-2 py-0.5 text-[11px] font-bold uppercase tracking-wide text-white">
                  Checked In
                </span>
                <span className="font-mono text-xs font-medium text-emerald-800">
                  {result.ticketCode}
                </span>
              </div>
              <p className="text-base font-bold text-emerald-950">{result.fullName}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onDismiss}
            aria-label="Dismiss notification"
            className="rounded-lg p-1 text-emerald-700 transition hover:bg-emerald-100"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      </div>
    );
  }

  if (result.outcome === 'ALREADY_CHECKED_IN') {
    return (
      <div className="fixed top-4 left-1/2 z-50 w-full max-w-md -translate-x-1/2 px-4">
        <div
          role="status"
          className="flex items-start justify-between gap-3 rounded-xl border-2 border-amber-400 bg-amber-50 p-4 text-amber-950 shadow-lg"
        >
          <div className="flex items-start gap-3">
            <AlertTriangle className="mt-0.5 h-6 w-6 shrink-0 text-amber-600" />
            <div className="space-y-0.5">
              <div className="flex items-center gap-2">
                <span className="inline-block rounded-md bg-amber-500 px-2 py-0.5 text-[11px] font-bold uppercase tracking-wide text-amber-950">
                  Already Checked In
                </span>
                <span className="font-mono text-xs font-medium text-amber-800">
                  {result.ticketCode}
                </span>
              </div>
              <p className="text-base font-bold text-amber-950">{result.fullName}</p>
              {result.checkedInAt && (
                <p className="text-xs font-medium text-amber-900">
                  First scanned: {formatDateTime(result.checkedInAt)}
                </p>
              )}
            </div>
          </div>
          <button
            type="button"
            onClick={onDismiss}
            aria-label="Dismiss notification"
            className="rounded-lg p-1 text-amber-700 transition hover:bg-amber-100"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed top-4 left-1/2 z-50 w-full max-w-md -translate-x-1/2 px-4">
      <div
        role="alert"
        className="flex items-start justify-between gap-3 rounded-xl border-2 border-red-400 bg-red-50 p-4 text-red-950 shadow-lg"
      >
        <div className="flex items-start gap-3">
          <XCircle className="mt-0.5 h-6 w-6 shrink-0 text-red-600" />
          <div className="space-y-0.5">
            <div className="flex items-center gap-2">
              <span className="inline-block rounded-md bg-red-600 px-2 py-0.5 text-[11px] font-bold uppercase tracking-wide text-white">
                {result.outcome === 'INVALID_TICKET' ? 'Invalid Ticket' : 'Not Confirmed'}
              </span>
              <span className="font-mono text-xs font-medium text-red-800">
                {result.ticketCode}
              </span>
            </div>
            <p className="text-sm font-semibold text-red-950">{result.message}</p>
          </div>
        </div>
        <button
          type="button"
          onClick={onDismiss}
          aria-label="Dismiss notification"
          className="rounded-lg p-1 text-red-700 transition hover:bg-red-100"
        >
          <X className="h-4 w-4" />
        </button>
      </div>
    </div>
  );
}