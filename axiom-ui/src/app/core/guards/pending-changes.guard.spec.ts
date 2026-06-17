import { Observable, of } from 'rxjs';
import { pendingChangesGuard } from './pending-changes.guard';

describe('pendingChangesGuard', () => {
  it('allows navigation when the component has no canDeactivate hook', () => {
    const result = pendingChangesGuard({} as never, {} as never, {} as never, {} as never);

    expect(result).toBe(true);
  });

  it('uses boolean component decisions', () => {
    const component = { canDeactivate: vi.fn(() => false) };

    const result = pendingChangesGuard(component, {} as never, {} as never, {} as never);

    expect(component.canDeactivate).toHaveBeenCalledOnce();
    expect(result).toBe(false);
  });

  it('passes observable component decisions through', () => {
    const decision$ = of(true);
    const component = { canDeactivate: vi.fn(() => decision$) };

    const result = pendingChangesGuard(component, {} as never, {} as never, {} as never);

    expect(result).toBe(decision$);
    expect(result).toBeInstanceOf(Observable);
  });

  it('passes promise component decisions through', async () => {
    const decision = Promise.resolve(true);
    const component = { canDeactivate: vi.fn(() => decision) };

    const result = pendingChangesGuard(component, {} as never, {} as never, {} as never);

    await expect(result).resolves.toBe(true);
  });
});
