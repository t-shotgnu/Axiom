import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';
import { Project, ProjectService } from '../../core/services/project.service';
import { ProjectSettingsComponent } from './project-settings';

describe('ProjectSettingsComponent', () => {
  let routeParamMap$: Subject<{ get: (key: string) => string | null }>;
  let currentProject$: BehaviorSubject<Project | null>;
  let projectService: {
    currentProject$: ReturnType<BehaviorSubject<Project | null>['asObservable']>;
    getProjectById: ReturnType<typeof vi.fn>;
    updateProject: ReturnType<typeof vi.fn>;
    setCurrentProject: ReturnType<typeof vi.fn>;
    deleteProject: ReturnType<typeof vi.fn>;
  };
  let router: { navigate: ReturnType<typeof vi.fn> };
  let component: ProjectSettingsComponent;
  let routeSnapshotId: string | null;
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  const project: Project = {
    id: 'project-1',
    name: 'Axiom',
    code: 'AX',
    description: 'Main project',
    createdOn: '2026-01-01',
    ownerId: 'user-1',
  };

  beforeEach(() => {
    routeParamMap$ = new Subject();
    currentProject$ = new BehaviorSubject<Project | null>(null);
    routeSnapshotId = 'project-1';
    projectService = {
      currentProject$: currentProject$.asObservable(),
      getProjectById: vi.fn(() => of(project)),
      updateProject: vi.fn(() => of(void 0)),
      setCurrentProject: vi.fn(),
      deleteProject: vi.fn(() => of(void 0)),
    };
    router = {
      navigate: vi.fn(() => Promise.resolve(true)),
    };
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    TestBed.configureTestingModule({
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: routeParamMap$.asObservable(),
            snapshot: {
              paramMap: {
                get: vi.fn(() => routeSnapshotId),
              },
            },
          },
        },
        { provide: Router, useValue: router },
        { provide: ProjectService, useValue: projectService },
      ],
    });

    component = TestBed.runInInjectionContext(() => new ProjectSettingsComponent());
  });

  afterEach(() => {
    consoleErrorSpy.mockRestore();
  });

  it('loads the project from route params and fills the form', () => {
    routeParamMap$.next({ get: () => 'project-1' });

    expect(projectService.getProjectById).toHaveBeenCalledWith('project-1');
    expect(component.project()).toEqual(project);
    expect(component.form).toEqual({ name: 'Axiom', code: 'AX' });
    expect(component.loadingProject()).toBe(false);
  });

  it('navigates to the settings page for a newly selected sidebar project', () => {
    currentProject$.next({ ...project, id: 'project-2' });

    expect(router.navigate).toHaveBeenCalledWith(['/projects', 'project-2', 'settings']);
  });

  it('does not navigate when the selected sidebar project matches the route', () => {
    currentProject$.next(project);

    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('blocks saving when project is missing, user cannot manage, or form is blank', () => {
    component.saveProjectDetails();

    component.project.set(project);
    component.canManageProject.set(false);
    component.saveProjectDetails();

    component.canManageProject.set(true);
    component.form = { name: '  ', code: 'AX' };
    component.saveProjectDetails();

    component.form = { name: 'Axiom', code: '  ' };
    component.saveProjectDetails();

    expect(projectService.updateProject).not.toHaveBeenCalled();
  });

  it('saves trimmed project details, reloads, and publishes the current project', () => {
    component.project.set(project);
    component.canManageProject.set(true);
    component.form = { name: '  Renamed project  ', code: ' rp ' };

    component.saveProjectDetails();

    expect(projectService.updateProject).toHaveBeenCalledWith('project-1', {
      name: 'Renamed project',
      code: 'RP',
    });
    expect(projectService.getProjectById).toHaveBeenCalledWith('project-1');
    expect(projectService.setCurrentProject).toHaveBeenCalledWith({
      ...project,
      name: 'Renamed project',
      code: 'RP',
    });
    expect(component.savingProject()).toBe(false);
    expect(component.projectSaveError()).toBe('');
  });

  it('shows a save error when updating fails', () => {
    projectService.updateProject.mockReturnValue(throwError(() => new Error('nope')));
    component.project.set(project);
    component.canManageProject.set(true);
    component.form = { name: 'Renamed project', code: 'RP' };

    component.saveProjectDetails();

    expect(component.savingProject()).toBe(false);
    expect(component.projectSaveError()).toBe(
      'Could not update project name or key. Check your permissions and try again.',
    );
  });

  it('opens and cancels the delete dialog', () => {
    component.projectDeleteError.set('Old error');

    component.openDeleteProjectDialog();

    expect(component.showDeleteDialog()).toBe(true);
    expect(component.projectDeleteError()).toBe('');

    component.cancelDeleteProjectDialog();

    expect(component.showDeleteDialog()).toBe(false);
    expect(component.projectDeleteError()).toBe('');
  });

  it('deletes a project and navigates back to projects', () => {
    component.project.set(project);
    component.showDeleteDialog.set(true);

    component.confirmDeleteProject();

    expect(projectService.deleteProject).toHaveBeenCalledWith('project-1');
    expect(component.showDeleteDialog()).toBe(false);
    expect(component.deletingProject()).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/projects']);
  });

  it('guards repeated or missing delete confirmation and maps delete errors', () => {
    component.confirmDeleteProject();
    expect(projectService.deleteProject).not.toHaveBeenCalled();

    component.project.set(project);
    component.deletingProject.set(true);
    component.confirmDeleteProject();
    expect(projectService.deleteProject).not.toHaveBeenCalled();

    component.deletingProject.set(false);
    projectService.deleteProject.mockReturnValue(throwError(() => new Error('nope')));
    component.confirmDeleteProject();

    expect(component.deletingProject()).toBe(false);
    expect(component.projectDeleteError()).toBe('Could not delete the project. Please try again.');
  });
});
