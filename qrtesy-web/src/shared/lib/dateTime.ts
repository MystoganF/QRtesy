const MANILA_TZ = 'Asia/Manila';

export function formatEventDate(isoString: string): string {
  return new Intl.DateTimeFormat('en-PH', {
    timeZone: MANILA_TZ,
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(isoString));
}

export function formatEventTime(isoString: string): string {
  return new Intl.DateTimeFormat('en-PH', {
    timeZone: MANILA_TZ,
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  }).format(new Date(isoString));
}

export function formatTimeRange(startsAt: string, endsAt: string): string {
  return `${formatEventTime(startsAt)} – ${formatEventTime(endsAt)} (PHT)`;
}

export function formatDateTime(isoString: string): string {
  return `${formatEventDate(isoString)} · ${formatEventTime(isoString)}`;
}