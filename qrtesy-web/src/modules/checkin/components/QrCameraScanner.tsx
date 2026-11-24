import { useEffect, useId, useRef, useState } from 'react';
import { Html5Qrcode } from 'html5-qrcode';
import { AlertCircle, Camera, CameraOff, Loader2 } from 'lucide-react';

interface QrCameraScannerProps {
  onScan: (decodedText: string) => void;
  disabled?: boolean;
}

const SCAN_COOLDOWN_MS = 3000;

export function QrCameraScanner({ onScan, disabled = false }: QrCameraScannerProps) {
  const rawId = useId();
  const readerElementId = `qr-reader-${rawId.replace(/[^a-zA-Z0-9_-]/g, '')}`;

  const [cameraActive, setCameraActive] = useState(false);
  const [starting, setStarting] = useState(false);
  const [cameraError, setCameraError] = useState<string | null>(null);

  const onScanRef = useRef(onScan);
  onScanRef.current = onScan;

  const disabledRef = useRef(disabled);
  disabledRef.current = disabled;

  const lastScanRef = useRef<{ value: string; timestamp: number }>({
    value: '',
    timestamp: 0,
  });

  useEffect(() => {
    if (!cameraActive) {
      return;
    }

    let cancelled = false;
    const scanner = new Html5Qrcode(readerElementId);

    const startScanner = async () => {
      setStarting(true);
      setCameraError(null);

      try {
        await scanner.start(
          { facingMode: 'environment' },
          {
            fps: 10,
            qrbox: { width: 220, height: 220 },
            aspectRatio: 1.333,
          },
          (decodedText) => {
            if (disabledRef.current) {
              return;
            }

            const cleaned = decodedText.trim();
            if (!cleaned) {
              return;
            }

            const now = Date.now();
            const isDuplicateWithinCooldown =
              lastScanRef.current.value === cleaned &&
              now - lastScanRef.current.timestamp < SCAN_COOLDOWN_MS;

            if (isDuplicateWithinCooldown) {
              return;
            }

            lastScanRef.current = { value: cleaned, timestamp: now };
            onScanRef.current(cleaned);
          },
          () => {
            // Ignore frame-by-frame decode misses while scanning
          }
        );

        if (!cancelled) {
          setStarting(false);
        }
      } catch (err) {
        if (!cancelled) {
          setStarting(false);
          setCameraActive(false);
          const message =
            err instanceof Error && err.message
              ? err.message
              : 'Unable to access camera. Check browser permissions or enter the ticket code manually.';
          setCameraError(message);
        }
      }
    };

    startScanner();

    return () => {
      cancelled = true;
      const stopAndClear = async () => {
        try {
          if (scanner.isScanning) {
            await scanner.stop();
          }
          scanner.clear();
        } catch {
          // Ignore teardown errors if stream already closed
        }
      };
      void stopAndClear();
    };
  }, [cameraActive, readerElementId]);

  return (
    <div className="rounded-xl border border-slate-200 bg-slate-50/70 p-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <p className="text-sm font-semibold text-slate-900">Camera QR Scanner</p>
          <p className="text-xs text-slate-500">
            Scan attendee entry passes directly or type the ticket code below.
          </p>
        </div>

        <button
          type="button"
          onClick={() => {
            setCameraError(null);
            setCameraActive((prev) => !prev);
          }}
          className={
            cameraActive
              ? 'inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-100'
              : 'inline-flex items-center gap-1.5 rounded-lg bg-slate-900 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-slate-800'
          }
        >
          {cameraActive ? (
            <>
              <CameraOff className="h-3.5 w-3.5" />
              <span>Stop Camera</span>
            </>
          ) : (
            <>
              <Camera className="h-3.5 w-3.5" />
              <span>Start Camera</span>
            </>
          )}
        </button>
      </div>

      {cameraError && (
        <div
          role="alert"
          className="mt-3 flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-xs font-medium text-red-800"
        >
          <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
          <span>{cameraError}</span>
        </div>
      )}

      {cameraActive && (
        <div className="relative mt-3 overflow-hidden rounded-xl border border-slate-200 bg-slate-950">
          {starting && (
            <div className="flex items-center justify-center gap-2 py-12 text-xs font-medium text-slate-300">
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>Initializing camera...</span>
            </div>
          )}
          <div id={readerElementId} className="w-full" />
        </div>
      )}
    </div>
  );
}