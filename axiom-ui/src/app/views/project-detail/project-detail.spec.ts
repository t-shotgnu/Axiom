import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { Project, ProjectService } from '../../core/services/project.service';
import { WorkItem, WorkItemService } from '../../core/services/work-item.service';
import { ProjectDetailComponent } from './project-detail';

describe('ProjectDetailComponent', () => {
  let projectService: {
    getProjectById: ReturnType<typeof vi.fn>;
  };
  let workItemService: {
    getWorkItems: ReturnType<typeof vi.fn>;
    createWorkItem: ReturnType<typeof vi.fn>;
  };
  let component: ProjectDetailComponent;

  const project: Project = {
    id: 'project-1',
    name: 'Axiom',
    code: 'AX',
    description: 'Main project',
    createdOn: '2026-01-01',
    ownerId: 'user-1',
  };

  const task: WorkItem = {
    id: 'task-1',
    controlNo: 1,
    description: 'Build tests',
    priority: 2,
    type: 'Task',
    status: 'New',
    dueDate: '',
    estimatedEffort: 3,
    projectId: 'project-1',
    authorId: 'user-1',
    assigneeId: '',
  };

  beforeEach(() => {
    projectService = {
      getProjectById: vi.fn(() => of(project)),
    };
    workItemService = {
      getWorkItems: vi.fn(() => of([task])),
      createWorkItem: vi.fn(() => of('task-2')),
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
        { provide: WorkItemService, useValue: workItemService },
      ],
    });

    component = TestBed.runInInjectionContext(() => new ProjectDetailComponent());
  });

  it('loads the project and tasks for the route project id', () => {
    expect(projectService.getProjectById).toHaveBeenCalledWith('project-1');
    expect(workItemService.getWorkItems).toHaveBeenCalledWith('project-1');
    expect(component.project()).toEqual(project);
    expect(component.tasks()).toEqual([task]);
    expect(component.loadingProject()).toBe(false);
    expect(component.loadingTasks()).toBe(false);
  });

  it('opens the create dialog with an empty default task', () => {
    component.newTask.description = 'Dirty value';

    component.openCreateDialog();

    expect(component.showCreateDialog()).toBe(true);
    expect(component.newTask).toEqual({
      description: '',
      priority: 1,
      type: 'Task',
      status: 'New',
    });
  });

  it('creates a task for the current project and reloads tasks', () => {
    component.newTask = {
      description: 'New task',
      priority: 3,
      type: 'Bug',
      status: 'New',
    };
    component.showCreateDialog.set(true);

    component.createTask();

    expect(workItemService.createWorkItem).toHaveBeenCalledWith({
      description: 'New task',
      priority: 3,
      type: 'Bug',
      status: 'New',
      projectId: 'project-1',
    });
    expect(component.creating()).toBe(false);
    expect(component.showCreateDialog()).toBe(false);
    expect(workItemService.getWorkItems).toHaveBeenCalledTimes(2);
  });

  it('maps status, type icon, and type color fallbacks', () => {
    expect(component.getStatusSeverity('Resolved')).toBe('success');
    expect(component.getStatusSeverity('Unknown')).toBe('secondary');
    expect(component.getTypeIcon('Bug')).toBe('pi pi-exclamation-circle');
    expect(component.getTypeIcon('Unknown')).toBe('pi pi-circle');
    expect(component.getTypeColor('Feature')).toBe('#3b82f6');
    expect(component.getTypeColor('Unknown')).toBe('#71717a');
  });
});
