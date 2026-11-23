import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, Loader2, Ticket } from 'lucide-react';
import { ApiError } from '@/shared/types/api';
import { registrationsApi } from '../../api/registrationsApi';

const registerSchema = z.object({
  fullName: z
    .string()
    .trim()
    .min(2, 'Full name must be at least 2 characters.')
    .max(120, 'Full name must not exceed 120 characters.'),
  email: z
    .string()
    .trim()
    .min(1, 'Email address is required.')
    .email('Please enter a valid email address.'),
  mobile: z
    .string()
    .trim()
    .regex(/^09\d{9}$/, 'Enter an 11-digit Philippine mobile number starting with 09 (e.g., 09171234567).'),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

interface RegisterFormProps {
  eventId: string;
  disabled: boolean;
}

export function RegisterForm({ eventId, disabled }: RegisterFormProps) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { fullName: '', email: '', mobile: '' },
  });

  const mutation = useMutation({
    mutationFn: (values: RegisterFormValues) =>
      registrationsApi.register({ eventId, ...values }),
    onSuccess: (ticket) => {
      queryClient.invalidateQueries({ queryKey: ['public-events'] });
      queryClient.invalidateQueries({ queryKey: ['event-details', eventId] });
      navigate(`/ticket/${encodeURIComponent(ticket.ticketCode)}`, {
        state: { ticket },
      });
    },
    onError: (err) => {
      if (err instanceof ApiError) {
        if (err.fieldErrors) {
          Object.entries(err.fieldErrors).forEach(([field, message]) => {
            if (field === 'fullName' || field === 'email' || field === 'mobile') {
              setError(field, { message });
            }
          });
        }
        setServerError(err.message);
      } else {
        setServerError('Something went wrong while registering. Please try again.');
      }
    },
  });

  const onSubmit = (values: RegisterFormValues) => {
    setServerError(null);
    mutation.mutate(values);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
      {serverError && (
        <div
          role="alert"
          className="flex items-start gap-2.5 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-800"
        >
          <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
          <span>{serverError}</span>
        </div>
      )}

      <div>
        <label htmlFor="fullName" className="block text-sm font-medium text-slate-700">
          Full Name
        </label>
        <input
          id="fullName"
          type="text"
          disabled={disabled || mutation.isPending}
          placeholder="Juan Dela Cruz"
          {...register('fullName')}
          className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-slate-900 focus:outline-none disabled:bg-slate-100 disabled:text-slate-500"
        />
        {errors.fullName && (
          <p className="mt-1 text-xs text-red-600">{errors.fullName.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="email" className="block text-sm font-medium text-slate-700">
          Email Address
        </label>
        <input
          id="email"
          type="email"
          disabled={disabled || mutation.isPending}
          placeholder="juan@example.com"
          {...register('email')}
          className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-slate-900 focus:outline-none disabled:bg-slate-100 disabled:text-slate-500"
        />
        {errors.email && (
          <p className="mt-1 text-xs text-red-600">{errors.email.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="mobile" className="block text-sm font-medium text-slate-700">
          Mobile Number (PH)
        </label>
        <input
          id="mobile"
          type="tel"
          inputMode="numeric"
          maxLength={11}
          disabled={disabled || mutation.isPending}
          placeholder="09171234567"
          {...register('mobile')}
          className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-slate-900 focus:outline-none disabled:bg-slate-100 disabled:text-slate-500"
        />
        {errors.mobile && (
          <p className="mt-1 text-xs text-red-600">{errors.mobile.message}</p>
        )}
      </div>

      <button
        type="submit"
        disabled={disabled || mutation.isPending}
        className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-300 disabled:text-slate-600"
      >
        {mutation.isPending ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin" />
            <span>Securing spot...</span>
          </>
        ) : disabled ? (
          <span>Event Full</span>
        ) : (
          <>
            <Ticket className="h-4 w-4" />
            <span>Register for Free</span>
          </>
        )}
      </button>
    </form>
  );
}