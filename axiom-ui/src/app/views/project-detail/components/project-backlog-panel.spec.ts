import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { ProjectMemberService } from '../../../core/services/project-member.service';
import { WorkItem, WorkItemService } from '../../../core/services/work-item.service';
import { ProjectBacklogPanelComponent } from './project-backlog-panel';

describe('ProjectBacklogPanelComponent', () => {
  let fixture: ComponentFixture<ProjectBacklogPanelComponent>;
  let component: ProjectBacklogPanelComponent;
  let workItemService: {
    getWorkItems: ReturnType<typeof vi.fn>;
    createWorkItem: ReturnType<typeof vi.fn>;
    deleteWorkItem: ReturnType<typeof vi.fn>;
  };
  let memberService: { getProjectMembers: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  const tasks: WorkItem[] = Array.from({ length: 12 }, (_, index) => ({
    id: `task-${index + 1}`,
    controlNo: index + 1,
    description: `Task ${index + 1}`,
    priority: 1,
    type: 'Task',
    status: index % 2 === 0 ? 'New' : 'Closed',
    projectId: 'project-1',
    authorId: 'user-1',
    assigneeId: index === 0 ? 'user-2' : null,
  }));

  beforeEach(async () => {
    workItemService = {
      getWorkItems: vi.fn(() => of(tasks)),
      createWorkItem: vi.fn(() => of('task-new')),
      deleteWorkItem: vi.fn(() => of(void 0)),
    };
    memberService = {
      getProjectMembers: vi.fn(() =>
        of([
          {
            userId: 'user-2',
            userName: 'grace',
            emailAddress: 'grace@example.com',
            firstName: 'Grace',
            lastName: 'Hopper',
            role: 'MEMBER',
            createdOn: '2026-01-01T12:00:00',
          },
          {
            userId: 'user-3',
            userName: 'linus',
            emailAddress: 'linus@example.com',
            role: 'ADMIN',
            createdOn: '2026-01-01T12:00:00',
          },
        ]),
      ),
    };
    router = { navigate: vi.fn(() => Promise.resolve(true)) };
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    await TestBed.configureTestingModule({
      imports: [ProjectBacklogPanelComponent],
      providers: [
        { provide: WorkItemService, useValue: workItemService },
        { provide: ProjectMemberService, useValue: memberService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: { snapshot: {}, queryParams: of({}), params: of({}) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectBacklogPanelComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    consoleErrorSpy.mockRestore();
  });

  function initialize(projectId = 'project-1'): void {
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();
  }

  it('loads tasks and members on init', () => {
    initialize();

    expect(workItemService.getWorkItems).toHaveBeenCalledWith('project-1');
    expect(memberService.getProjectMembers).toHaveBeenCalledWith('project-1');
    expect(component.tasks()).toEqual(tasks);
    expect(component.projectUsers()).toEqual([
      { id: 'user-2', userName: 'grace', fullName: 'Grace Hopper' },
      { id: 'user-3', userName: 'linus', fullName: 'linus' },
    ]);
    expect(component.loadingTasks()).toBe(false);
  });

  it('reloads tasks and members when project id changes after initialization', () => {
    initialize('project-1');
    workItemService.getWorkItems.mockClear();
    memberService.getProjectMembers.mockClear();

    fixture.componentRef.setInput('projectId', 'project-2');

    expect(workItemService.getWorkItems).toHaveBeenCalledWith('project-2');
    expect(memberService.getProjectMembers).toHaveBeenCalledWith('project-2');
  });

  it('does not load tasks without a project and clears member options', () => {
    component.loadTasks();
    component.loadMembers();

    expect(workItemService.getWorkItems).not.toHaveBeenCalled();
    expect(component.projectUsers()).toEqual([]);
  });

  it('paginates tasks and clamps page changes', () => {
    initialize();

    expect(component.totalPages()).toBe(2);
    expect(component.pagedTasks()).toHaveLength(10);
    expect(component.firstVisibleTaskIndex()).toBe(1);
    expect(component.lastVisibleTaskIndex()).toBe(10);
    expect(component.getPagesArray()).toEqual([1, 2]);

    component.goToPage(99);

    expect(component.currentPage()).toBe(2);
    expect(component.pagedTasks()).toHaveLength(2);
    expect(component.firstVisibleTaskIndex()).toBe(11);
    expect(component.lastVisibleTaskIndex()).toBe(12);

    component.goToPage(-1);
    expect(component.currentPage()).toBe(1);
  });

  it('returns compact page arrays for large task lists', () => {
    component.tasks.set(Array.from({ length: 100 }, (_, index) => ({ ...tasks[0], id: `task-${index}` })));
    component.currentPage.set(5);

    expect(component.totalPages()).toBe(10);
    expect(component.getPagesArray()).toEqual([1, 3, 4, 5, 6, 7, 10]);
  });

  it('opens the create dialog with an empty task template', () => {
    component.newTask = { description: 'Old task', priority: 5, type: 'Bug', status: 'Closed' };

    component.openCreateDialog();

    expect(component.showCreateDialog()).toBe(true);
    expect(component.newTask).toEqual({
      description: '',
      priority: 1,
      type: 'Task',
      status: 'New',
      assigneeId: '',
    });
  });

  it('navigates to the full task creation route', () => {
    initialize();

    component.navigateToCreate();

    expect(router.navigate).toHaveBeenCalledWith(['/tasks/new'], {
      queryParams: { projectId: 'project-1' },
    });
  });

  it('creates a trimmed task, normalizes blank assignee, closes dialog, and reloads tasks', () => {
    initialize();
    workItemService.getWorkItems.mockClear();
    component.showCreateDialog.set(true);
    component.newTask = {
      description: '  New task  ',
      priority: 3,
      type: 'Bug',
      status: 'New',
      assigneeId: '   ',
    };

    component.createTask();

    expect(workItemService.createWorkItem).toHaveBeenCalledWith({
      description: 'New task',
      priority: 3,
      type: 'Bug',
      status: 'New',
      assigneeId: null,
      projectId: 'project-1',
    });
    expect(component.creating()).toBe(false);
    expect(component.showCreateDialog()).toBe(false);
    expect(workItemService.getWorkItems).toHaveBeenCalledWith('project-1');
  });

  it('does not create a task without project id or description', () => {
    component.newTask = { description: 'Task', priority: 1, type: 'Task', status: 'New' };
    component.createTask();

    component.projectId = 'project-1';
    component.newTask = { description: '   ', priority: 1, type: 'Task', status: 'New' };
    component.createTask();

    expect(workItemService.createWorkItem).not.toHaveBeenCalled();
  });

  it('stops creating when create task fails', () => {
    initialize();
    workItemService.createWorkItem.mockReturnValue(throwError(() => new Error('nope')));
    component.newTask = { description: 'Task', priority: 1, type: 'Task', status: 'New' };

    component.createTask();

    expect(component.creating()).toBe(false);
    expect(consoleErrorSpy).toHaveBeenCalledWith(expect.any(Error));
  });

  it('opens, cancels, and confirms task deletion', () => {
    initialize();
    workItemService.getWorkItems.mockClear();
    const event = {
      preventDefault: vi.fn(),
      stopPropagation: vi.fn(),
    } as unknown as MouseEvent;

    component.openDeleteTaskDialog(tasks[0], event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(event.stopPropagation).toHaveBeenCalled();
    expect(component.taskToDelete()).toEqual(tasks[0]);
    expect(component.showDeleteTaskDialog()).toBe(true);

    component.cancelDeleteTaskDialog();

    expect(component.taskToDelete()).toBeNull();
    expect(component.showDeleteTaskDialog()).toBe(false);

    component.openDeleteTaskDialog(tasks[0]);
    component.confirmDeleteTask();

    expect(workItemService.deleteWorkItem).toHaveBeenCalledWith('task-1');
    expect(component.taskToDelete()).toBeNull();
    expect(component.deleting()).toBe(false);
    expect(workItemService.getWorkItems).toHaveBeenCalledWith('project-1');
  });

  it('guards repeated or missing delete confirmation', () => {
    component.confirmDeleteTask();
    expect(workItemService.deleteWorkItem).not.toHaveBeenCalled();

    component.taskToDelete.set(tasks[0]);
    component.deleting.set(true);
    component.confirmDeleteTask();

    expect(workItemService.deleteWorkItem).not.toHaveBeenCalled();
  });

  it('maps delete errors from status codes and API messages', () => {
    initialize();
    const cases: Array<[unknown, string]> = [
      [
        new HttpErrorResponse({ status: 401 }),
        'Your session expired. Sign in again, then retry deleting the issue.',
      ],
      [new HttpErrorResponse({ status: 403 }), 'You are not allowed to delete this issue.'],
      [new HttpErrorResponse({ status: 0 }), 'Could not reach the API. Check your network connection.'],
      [new HttpErrorResponse({ status: 400, error: { detail: 'Cannot delete parent task.' } }), 'Cannot delete parent task.'],
      [new HttpErrorResponse({ status: 400, error: { message: 'Task has blockers.' } }), 'Task has blockers.'],
      [new HttpErrorResponse({ status: 400, error: 'Plain API error' }), 'Plain API error'],
      [new Error('unknown'), 'Could not delete the issue. Please try again.'],
    ];

    for (const [error, expectedMessage] of cases) {
      workItemService.deleteWorkItem.mockReturnValueOnce(throwError(() => error));
      component.taskToDelete.set(tasks[0]);

      component.confirmDeleteTask();

      expect(component.deleteErrorMessage()).toBe(expectedMessage);
      expect(component.deleting()).toBe(false);
    }
  });

  it('formats status and assignee labels', () => {
    initialize();

    expect(component.getStatusLabel('in_progress')).toBe('IN PROGRESS');
    expect(component.getStatusLabel('InDevelopment')).toBe('IN DEVELOPMENT');
    expect(component.getStatusLabel('InTesting')).toBe('IN TESTING');
    expect(component.getStatusLabel('New')).toBe('TO DO');
    expect(component.getStatusLabel('Resolved')).toBe('REVIEW');
    expect(component.getStatusLabel('Closed')).toBe('DONE');
    expect(component.getStatusLabel('Blocked')).toBe('BLOCKED');

    expect(component.getAssigneeName(tasks[0])).toBe('Grace Hopper');
    expect(component.getAssigneeName({ ...tasks[0], assigneeId: 'missing-user' })).toBe('missing-user');
    expect(component.getAssigneeName({ ...tasks[0], assigneeId: null })).toBe('Unassigned');
  });

  it('keeps loading true until task loading finalizes and logs load errors', () => {
    const taskRequest$ = new Subject<WorkItem[]>();
    workItemService.getWorkItems.mockReturnValue(taskRequest$.asObservable());
    initialize();

    expect(component.loadingTasks()).toBe(true);

    taskRequest$.next(tasks);
    taskRequest$.complete();

    expect(component.loadingTasks()).toBe(false);

    workItemService.getWorkItems.mockReturnValue(throwError(() => new Error('load failed')));
    component.loadTasks();

    expect(component.loadingTasks()).toBe(false);
    expect(consoleErrorSpy).toHaveBeenCalledWith(expect.any(Error));
  });
});
