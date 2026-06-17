import { ChangeDetectorRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject, throwError } from 'rxjs';
import { CreateProjectComponent } from './create-project';
import { ProjectService } from '../../core/services/project.service';

describe('CreateProjectComponent', () => {
  let projectService: { createProject: ReturnType<typeof vi.fn> };
  let cdr: { markForCheck: ReturnType<typeof vi.fn> };
  let component: CreateProjectComponent;

  beforeEach(() => {
    projectService = {
      createProject: vi.fn(() => of('project-1')),
    };
    cdr = { markForCheck: vi.fn() };
    component = new CreateProjectComponent(
      projectService as unknown as ProjectService,
      cdr as unknown as ChangeDetectorRef,
    );
  });

  it('shows a clean create dialog', () => {
    component.form = { name: 'Old', code: 'old', description: 'Old description' };
    component.createErrorMessage = 'Old error';

    component.show();

    expect(component.displayCreateDialog).toBe(true);
    expect(component.form).toEqual({ name: '', code: '', description: '' });
    expect(component.createErrorMessage).toBe('');
    expect(cdr.markForCheck).toHaveBeenCalled();
  });

  it('does not submit blank name or code', () => {
    component.form = { name: '   ', code: 'AX', description: '' };
    component.createProject();

    component.form = { name: 'Axiom', code: '   ', description: '' };
    component.createProject();

    expect(projectService.createProject).not.toHaveBeenCalled();
  });

  it('trims fields, uppercases code, closes, and emits after creation', () => {
    const created: unknown[] = [];
    component.projectCreated.subscribe((value) => created.push(value));
    component.form = { name: '  Axiom  ', code: ' ax ', description: '  Main project  ' };

    component.createProject();

    expect(projectService.createProject).toHaveBeenCalledWith({
      name: 'Axiom',
      code: 'AX',
      description: 'Main project',
    });
    expect(component.displayCreateDialog).toBe(false);
    expect(component.form).toEqual({ name: '', code: '', description: '' });
    expect(created).toEqual([undefined]);
  });

  it('omits empty descriptions', () => {
    component.form = { name: 'Axiom', code: 'AX', description: '   ' };

    component.createProject();

    expect(projectService.createProject).toHaveBeenCalledWith({ name: 'Axiom', code: 'AX' });
  });

  it('keeps creating true until the request finalizes', () => {
    const request$ = new Subject<string>();
    projectService.createProject.mockReturnValue(request$.asObservable());
    component.form = { name: 'Axiom', code: 'AX', description: '' };

    component.createProject();

    expect(component.creating).toBe(true);

    request$.next('project-1');
    request$.complete();

    expect(component.creating).toBe(false);
    expect(cdr.markForCheck).toHaveBeenCalled();
  });

  it('maps common API errors to friendly messages', () => {
    const cases: Array<[HttpErrorResponse, string]> = [
      [
        new HttpErrorResponse({ status: 401 }),
        'Your session expired or you are not allowed to create projects. Sign in again, then retry.',
      ],
      [new HttpErrorResponse({ status: 409 }), 'A project with this code may already exist. Try another code.'],
      [
        new HttpErrorResponse({ status: 400, error: { detail: 'Name is required.' } }),
        'Name is required.',
      ],
      [
        new HttpErrorResponse({ status: 400, error: { message: 'Code is required.' } }),
        'Code is required.',
      ],
      [new HttpErrorResponse({ status: 400, error: 'Plain API error' }), 'Plain API error'],
      [
        new HttpErrorResponse({ status: 0 }),
        'Could not reach the API. Check your network or that the backend is running.',
      ],
    ];

    for (const [error, expectedMessage] of cases) {
      projectService.createProject.mockReturnValueOnce(throwError(() => error));
      component.form = { name: 'Axiom', code: 'AX', description: '' };

      component.createProject();

      expect(component.createErrorMessage).toBe(expectedMessage);
    }
  });
});
