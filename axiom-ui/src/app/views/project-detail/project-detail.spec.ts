import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { Project, ProjectService } from '../../core/services/project.service';
import { ProjectDetailComponent } from './project-detail';

describe('ProjectDetailComponent', () => {
  let projectService: {
    getProjectById: ReturnType<typeof vi.fn>;
    deleteProject: ReturnType<typeof vi.fn>;
  };
  let router: {
    navigate: ReturnType<typeof vi.fn>;
  };
  let component: ProjectDetailComponent;

  const project: Project = {
    id: 'project-1',
    name: 'Axiom',
    code: 'AX',
    description: 'Main project',
    createdOn: '2026-01-01',
    ownerId: 'user-1',
    ownerName: 'Jane Doe',
  };

  beforeEach(() => {
    projectService = {
      getProjectById: vi.fn(() => of(project)),
      deleteProject: vi.fn(() => of(void 0)),
    };
    router = {
      navigate: vi.fn(() => Promise.resolve(true)),
    };

    TestBed.configureTestingModule({
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: vi.fn(() => 'project-1'),
              },
            },
          },
        },
        { provide: ProjectService, useValue: projectService },
        { provide: Router, useValue: router },
      ],
    });

    component = TestBed.runInInjectionContext(() => new ProjectDetailComponent());
  });

  it('loads the project and tasks for the route project id', () => {
    expect(projectService.getProjectById).toHaveBeenCalledWith('project-1');
    expect(component.project()).toEqual(project);
    expect(component.loadingProject()).toBe(false);
  });

  it('opens the project delete dialog and confirms deletion', () => {
    component.canManageProject.set(true);
    component.openDeleteProjectDialog();

    expect(component.showDeleteProjectDialog()).toBe(true);

    component.confirmDeleteProject();

    expect(projectService.deleteProject).toHaveBeenCalledWith('project-1');
  });
});
