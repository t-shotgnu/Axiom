import { extractErrorMessage } from './error-utils';

describe('extractErrorMessage', () => {
  it('prefers ProblemDetail detail fields', () => {
    expect(extractErrorMessage({ error: { detail: 'Precise API detail' } }, 'Fallback')).toBe(
      'Precise API detail',
    );
  });

  it('uses API message fields when detail is absent', () => {
    expect(extractErrorMessage({ error: { message: 'Message field' } }, 'Fallback')).toBe(
      'Message field',
    );
  });

  it('uses the thrown error message before the fallback', () => {
    expect(extractErrorMessage({ message: 'Network broke' }, 'Fallback')).toBe('Network broke');
  });

  it('returns the fallback for unknown shapes', () => {
    expect(extractErrorMessage({ error: { detail: 123 } }, 'Fallback')).toBe('Fallback');
    expect(extractErrorMessage(null, 'Fallback')).toBe('Fallback');
  });
});
