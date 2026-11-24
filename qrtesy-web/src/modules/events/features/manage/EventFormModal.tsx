import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { AlertCircle, Loader2, Save, X } from 'lucide-react';
import { ApiError } from '@/shared/types/api';
import { eventsApi } from '../../api/eventsApi';
import type { EventStatus, StaffEventItem } from '../../types/event.types';

const eventFormSchema = z
  .object({
    title: z.string().trim().min(2, 'Title is required.').max(160, 'Max 160 characters.'),
    description: z.string().trim().min(5, 'Description is required.'),
    venue: z.string().trim().min(2, 'Venue is required.').max(200, 'Max 200 characters.'),
    eventDate: z.string().min(1, 'Date is required.'),
    startTime: z.string().min(1, 'Start time is required.'),
    endTime: z.string().min(1, 'End time is required.'),
    capacity: z
      .number()
      .refine((val) => !Number.isNaN(val), 'Capacity is required.')
      .int('Capacity must be a whole number.')
      .min(1, 'Capacity must be at least 1.'),
    status: z.enum(['DRAFT', 'PUBLISHED', 'CLOSED']),
  })
  .refine((data) => data.endTime > data.startTime, {
    message: 'End time must be after start time.',
    path: ['endTime'],
  });

type EventFormValues = z.infer<typeof eventFormSchema>;

interface EventFormModalProps {
  editingEvent: StaffEventItem | null;
  onClose: () => void;
}

function normalizeTimeInput(raw?: string): string {
  if (!raw) return '13:00';
  return raw.slice(0, 5);
}

export function EventFormModal({ editingEvent, onClose }: EventFormModalProps) {
  const queryClient = useQueryClient();
  const [serverError, setServerError] = useState<string | null>(null);

  const hasRegistrations = Boolean(
    editingEvent && (editingEvent.occupiedCount > 0 || editingEvent.waitlistCount > 0)
  );

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EventFormValues>({
    resolver: zodResolver(eventFormSchema),
    defaultValues: editingEvent
      ? {
          title: editingEvent.title,
          description: editingEvent.description,
          venue: editingEvent.venue,
          eventDate: editingEvent.eventDate,
          startTime: normalizeTimeInput(editingEvent.startTime),
          endTime: normalizeTimeInput(editingEvent.endTime),
          capacity: editingEvent.capacity,
          status: editingEvent.status,
        }
      : {
          title: '',
          description: '',
          venue: '',
          eventDate: '',
          startTime: '13:00',
          endTime: '17:00',
          capacity: 30,
          status: 'DRAFT' as EventStatus,
        },
  });

  const mutation = useMutation({
    mutationFn: (values: EventFormValues) =>
      editingEvent
        ? eventsApi.updateEvent(editingEvent.id, values)
        : eventsApi.createEvent(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['staff-events'] });
      queryClient.invalidateQueries({ queryKey: ['staff-dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['staff-audit-logs'] });
      queryClient.invalidateQueries({ queryKey: ['public-events'] });
      if (editingEvent) {
        queryClient.invalidateQueries({ queryKey: ['event-details', editingEvent.id] });
      }
      onClose();
    },
    onError: (err) => {
      if (err instanceof ApiError) {
        setServerError(err.message);
      } else {
        setServerError('Unable to save event right now.');
      }
    },
  });

  const onSubmit = (values: EventFormValues) => {
    setServerError(null);
    mutation.mutate(values);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
      <div className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-slate-200 bg-white p-6 shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-100 pb-4">
          <h2 className="text-lg font-bold text-slate-900">
            {editingEvent ? 'Edit Event' : 'Create Event'}
          </h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close modal"
            className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 hover:text-slate-900"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-4 space-y-4">
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
            <label htmlFor="evTitle" className="block text-sm font-medium text-slate-700">
              Event Title
            </label>
            <input
              id="evTitle"
              type="text"
              {...register('title')}
              placeholder="Cebu Cloud & Backend Workshop"
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
            />
            {errors.title && <p className="mt-1 text-xs text-red-600">{errors.title.message}</p>}
          </div>

          <div>
            <label htmlFor="evDescription" className="block text-sm font-medium text-slate-700">
              Description
            </label>
            <textarea
              id="evDescription"
              rows={3}
              {...register('description')}
              placeholder="Session details, speakers, and agenda..."
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
            />
            {errors.description && (
              <p className="mt-1 text-xs text-red-600">{errors.description.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="evVenue" className="block text-sm font-medium text-slate-700">
              Venue
            </label>
            <input
              id="evVenue"
              type="text"
              {...register('venue')}
              placeholder="Ayala Center Cebu, Function Room 2"
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
            />
            {errors.venue && <p className="mt-1 text-xs text-red-600">{errors.venue.message}</p>}
          </div>

          <div className="grid gap-3 sm:grid-cols-3">
            <div>
              <label htmlFor="evDate" className="block text-sm font-medium text-slate-700">
                Date (PHT)
              </label>
              <input
                id="evDate"
                type="date"
                {...register('eventDate')}
                className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
              />
              {errors.eventDate && (
                <p className="mt-1 text-xs text-red-600">{errors.eventDate.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="evStart" className="block text-sm font-medium text-slate-700">
                Start Time
              </label>
              <input
                id="evStart"
                type="time"
                {...register('startTime')}
                className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
              />
              {errors.startTime && (
                <p className="mt-1 text-xs text-red-600">{errors.startTime.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="evEnd" className="block text-sm font-medium text-slate-700">
                End Time
              </label>
              <input
                id="evEnd"
                type="time"
                {...register('endTime')}
                className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
              />
              {errors.endTime && (
                <p className="mt-1 text-xs text-red-600">{errors.endTime.message}</p>
              )}
            </div>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div>
              <label htmlFor="evCapacity" className="block text-sm font-medium text-slate-700">
                Capacity
                {editingEvent && (
                  <span className="ml-1 text-xs font-normal text-slate-500">
                    ({editingEvent.occupiedCount} registered)
                  </span>
                )}
              </label>
              <input
                id="evCapacity"
                type="number"
                min={1}
                {...register('capacity', { valueAsNumber: true })}
                className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
              />
              {errors.capacity && (
                <p className="mt-1 text-xs text-red-600">{errors.capacity.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="evStatus" className="block text-sm font-medium text-slate-700">
                Status
              </label>
              <select
                id="evStatus"
                {...register('status')}
                className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-slate-900 focus:outline-none"
              >
                <option value="DRAFT" disabled={hasRegistrations}>
                  Draft {hasRegistrations ? '(Unavailable with registrations)' : '(Hidden from public)'}
                </option>
                <option value="PUBLISHED">Published (Open to public)</option>
                <option value="CLOSED">Closed (Registrations stopped)</option>
              </select>
            </div>
          </div>

          <div className="flex items-center justify-end gap-2 border-t border-slate-100 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={mutation.isPending}
              className="inline-flex items-center gap-2 rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:bg-slate-300"
            >
              {mutation.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Save className="h-4 w-4" />
              )}
              <span>{editingEvent ? 'Save Changes' : 'Create Event'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}