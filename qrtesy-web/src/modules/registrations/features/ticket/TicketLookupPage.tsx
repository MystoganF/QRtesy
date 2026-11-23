import { useState, type FormEvent } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { AlertCircle, Loader2, Search } from 'lucide-react';
import { ApiError } from '@/shared/types/api';
import { registrationsApi } from '../../api/registrationsApi';
import { TicketPassCard } from './TicketPassCard';
import type { RegistrationTicket } from '../../types/registration.types';

export function TicketLookupPage() {
  const [searchParams] = useSearchParams();
  const [ticketCode, setTicketCode] = useState(searchParams.get('code') ?? '');
  const [email, setEmail] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [foundTicket, setFoundTicket] = useState<RegistrationTicket | null>(null);

  const mutation = useMutation({
    mutationFn: () => registrationsApi.lookup(ticketCode, email),
    onSuccess: (data) => {
      setErrorMessage(null);
      setFoundTicket(data);
    },
    onError: (err) => {
      setFoundTicket(null);
      if (err instanceof ApiError && err.status === 404) {
        setErrorMessage('Ticket not found');
      } else if (err instanceof ApiError) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Unable to look up ticket right now.');
      }
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!ticketCode.trim() || !email.trim()) {
      setErrorMessage('Please enter both your ticket code and email address.');
      return;
    }
    setErrorMessage(null);
    mutation.mutate();
  };

  return (
    <div className="space-y-8">
      <div className="mx-auto max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
        <h1 className="text-xl font-bold text-slate-900">Find Your Ticket</h1>
        <p className="mt-1 text-sm text-slate-600">
          Enter your ticket code and the email address used during registration.
        </p>

        <form onSubmit={handleSubmit} className="mt-5 space-y-4">
          {errorMessage && (
            <div
              role="alert"
              className="flex items-center gap-2.5 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-800"
            >
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{errorMessage}</span>
            </div>
          )}

          <div>
            <label htmlFor="lookupCode" className="block text-sm font-medium text-slate-700">
              Ticket Code
            </label>
            <input
              id="lookupCode"
              type="text"
              value={ticketCode}
              onChange={(e) => setTicketCode(e.target.value.toUpperCase())}
              placeholder="QRT-ZTTZZ5"
              className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 font-mono text-sm uppercase text-slate-900 placeholder:font-sans placeholder:normal-case placeholder:text-slate-400 focus:border-slate-900 focus:outline-none"
            />
          </div>

          <div>
            <label htmlFor="lookupEmail" className="block text-sm font-medium text-slate-700">
              Registered Email
            </label>
            <input
              id="lookupEmail"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="juan@example.com"
              className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-slate-900 focus:outline-none"
            />
          </div>

          <button
            type="submit"
            disabled={mutation.isPending}
            className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:bg-slate-300"
          >
            {mutation.isPending ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                <span>Searching...</span>
              </>
            ) : (
              <>
                <Search className="h-4 w-4" />
                <span>View Ticket Pass</span>
              </>
            )}
          </button>
        </form>
      </div>

      {foundTicket && <TicketPassCard ticket={foundTicket} />}
    </div>
  );
}