import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { AlertCircle, ArrowLeft, Loader2, Search } from 'lucide-react';
import { ApiError } from '@/shared/types/api';
import { registrationsApi } from '../../api/registrationsApi';
import type { RegistrationTicket } from '../../types/registration.types';
import { TicketPassCard } from './TicketPassCard';

const lookupSchema = z.object({
  ticketCode: z.string().trim().min(1, 'Ticket code is required.'),
  email: z
    .string()
    .trim()
    .min(1, 'Email address is required.')
    .email('Please enter a valid email address.'),
});

type LookupFormValues = z.infer<typeof lookupSchema>;

export function TicketLookupPage() {
  const [searchParams] = useSearchParams();
  const [ticket, setTicket] = useState<RegistrationTicket | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LookupFormValues>({
    resolver: zodResolver(lookupSchema),
    defaultValues: {
      ticketCode: searchParams.get('code') ?? '',
      email: '',
    },
  });

  const mutation = useMutation({
    mutationFn: (values: LookupFormValues) =>
      registrationsApi.lookup(values.ticketCode, values.email),
    onSuccess: (found) => {
      setServerError(null);
      setTicket(found);
    },
    onError: (err) => {
      setTicket(null);
      if (err instanceof ApiError && err.status === 404) {
        setServerError('Ticket not found');
      } else if (err instanceof ApiError) {
        setServerError(err.message);
      } else {
        setServerError('Unable to look up ticket right now.');
      }
    },
  });

  const onSubmit = (values: LookupFormValues) => {
    setServerError(null);
    mutation.mutate(values);
  };

  return (
    <div className="space-y-6">
      <Link
        to="/"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to events
      </Link>

      <div className="mx-auto max-w-md rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
        <h1 className="text-xl font-bold text-slate-900">Find Your Ticket</h1>
        <p className="mt-1 text-xs text-slate-500">
          Enter your ticket code and the email address used during registration.
        </p>

        <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-4 space-y-4">
          {serverError && (
            <div
              role="alert"
              className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 p-3 text-sm font-medium text-red-800"
            >
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{serverError}</span>
            </div>
          )}

          <div>
            <label htmlFor="lookupCode" className="block text-sm font-medium text-slate-700">
              Ticket Code
            </label>
            <input
              id="lookupCode"
              type="text"
              placeholder="QRT-XXXXXX"
              {...register('ticketCode')}
              className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 font-mono text-sm uppercase text-slate-900 placeholder:font-sans placeholder:normal-case placeholder:text-slate-400 focus:border-slate-900 focus:outline-none"
            />
            {errors.ticketCode && (
              <p className="mt-1 text-xs text-red-600">{errors.ticketCode.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="lookupEmail" className="block text-sm font-medium text-slate-700">
              Registered Email
            </label>
            <input
              id="lookupEmail"
              type="email"
              placeholder="juan@example.com"
              {...register('email')}
              className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-slate-900 focus:outline-none"
            />
            {errors.email && (
              <p className="mt-1 text-xs text-red-600">{errors.email.message}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={mutation.isPending}
            className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:bg-slate-300"
          >
            {mutation.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Search className="h-4 w-4" />
            )}
            <span>View Ticket Pass</span>
          </button>
        </form>
      </div>

      {ticket && <TicketPassCard ticket={ticket} onTicketUpdated={setTicket} />}
    </div>
  );
}