/**
 * Extracts a human-readable message from an Angular HttpErrorResponse.
 * The backend returns RFC 7807 ProblemDetail objects with a `detail` field.
 */
export function extractErrorMessage(err: any, fallback: string): string {
  // RFC 7807 ProblemDetail: { detail: string, title: string, status: number }
  if (err?.error?.detail && typeof err.error.detail === 'string') {
    return err.error.detail;
  }
  if (err?.error?.message && typeof err.error.message === 'string') {
    return err.error.message;
  }
  if (err?.message && typeof err.message === 'string') {
    return err.message;
  }
  return fallback;
}
