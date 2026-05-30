import { ChangeDetectorRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject, of, throwError } from 'rxjs';
import { Project, ProjectService } from '../../core/services/project.service';
import { WorkItemService } from '../../core/services/work-item.service';
import { ProjectsComponent } from './projects';

describe('ProjectsComponent', () => {
  let projectService: {
    getAllProjects: ReturnType<typeof vi.fn>;
    createProject: ReturnType<typeof vi.fn>;
  };
  let workItemService: {
    getWorkItems: ReturnType<typeof vi.fn>;
  };
  let cdr: { markForCheck: ReturnType<typeof vi.fn> };

  function createComponent(loadProjects$ = new Subject<Project[]>()) {
    projectService = {
      getAllProjects: vi.fn(() => loadProjects$.asObservable()),
      createProject: vi.fn(),
    };
    workItemService = {
      getWorkItems: vi.fn(() => of([])),
    };
    cdr = {
      markForCheck: vi.fn(),
    };

    const component = new ProjectsComponent(
      projectService as unknown as ProjectService,
      workItemService as unknown as WorkItemService,
      cdr as unknown as ChangeDetectorRef,
    );

    return { component, loadProjects$ };
  }

  it('starts in loading state and renders a list after projects load', () => {
    const { component, loadProjects$ } = createComponent();

    expect(component.loading).toBe(true);
    expect(component.listState).toBe('loading');

    loadProjects$.next([
      {
        id: 'project-1',
        name: 'Axiom',
        code: 'AX',
        description: 'Main project',
        createdOn: '2026-01-01',
        ownerId: 'user-1',
      },
    ]);
    loadProjects$.complete();

    expect(component.loading).toBe(false);
    expect(component.projects).toHaveLength(1);
    expect(component.listState).toBe('list');
    expect(cdr.markForCheck).toHaveBeenCalled();
  });

  it('switches to empty state when no projects are returned', () => {
    const { component, loadProjects$ } = createComponent();

    loadProjects$.next([]);
    loadProjects$.complete();

    expect(component.listState).toBe('empty');
  });

  it('maps authorization errors while loading projects', () => {
    const { component, loadProjects$ } = createComponent();

    loadProjects$.error(new HttpErrorResponse({ status: 403 }));

    expect(component.loading).toBe(false);
    expect(component.listState).toBe('error');
    expect(component.errorMessage).toBe(
      'You need to sign in to load projects. Sign in, then refresh this page.',
    );
  });

  it('maps API detail messages while loading projects', () => {
    const { component, loadProjects$ } = createComponent();

    loadProjects$.error(
      new HttpErrorResponse({
        status: 500,
        error: { detail: 'Backend said no.' },
      }),
    );

    expect(component.errorMessage).toBe('Backend said no.');
  });
  // Creation UI moved into CreateProjectComponent; creation tests belong there.
});
