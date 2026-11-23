import { Link, NavLink, Outlet } from 'react-router-dom';
import { QrCode, Search, ShieldCheck } from 'lucide-react';
import { cn } from '@/shared/lib/cn';

export function AppShell() {
  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    cn(
      'inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium transition-colors',
      isActive
        ? 'bg-slate-900 text-white'
        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
    );

  return (
    <div className="min-h-screen flex flex-col">
      <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/90 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
          <Link
            to="/"
            className="inline-flex items-center gap-2 font-semibold tracking-tight text-slate-900"
          >
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-900 text-white">
              <QrCode className="h-4 w-4" />
            </span>
            <span>QRtesy</span>
          </Link>

          <nav className="flex items-center gap-1">
            <NavLink to="/ticket/lookup" className={navLinkClass}>
              <Search className="h-4 w-4" />
              <span className="hidden sm:inline">Find Ticket</span>
            </NavLink>
            <NavLink to="/staff/login" className={navLinkClass}>
              <ShieldCheck className="h-4 w-4" />
              <span className="hidden sm:inline">Staff</span>
            </NavLink>
          </nav>
        </div>
      </header>

      <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-6 sm:py-8">
        <Outlet />
      </main>
    </div>
  );
}