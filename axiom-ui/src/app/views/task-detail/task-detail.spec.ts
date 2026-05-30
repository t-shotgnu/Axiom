import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { WorkItem, WorkItemService } from '../../core/services/work-item.service';
import { TaskDetailComponent } from './task-detail';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ProjectService } from '../../core/services/project.service';
import { UserService } from '../../core/services/user.service';
import { ProjectMemberService } from '../../core/services/project-member.service';
import { CommentService } from '../../core/services/comment.service';
import { AttachmentService } from '../../core/services/attachment.service';

describe('TaskDetailComponent', () => {
  let workItemService: {
    getWorkItemById: ReturnType<typeof vi.fn>;
    updateWorkItemStatus: ReturnType<typeof vi.fn>;
    assignWorkItem: ReturnType<typeof vi.fn>;
  };
  let fixture: ComponentFixture<TaskDetailComponent>;
  let component: TaskDetailComponent;

  const task: WorkItem = {
    id: 'task-1',
    controlNo: 1,
    description: 'Build tests',
    priority: 2,
    type: 'Task',
    status: 'Active',
    dueDate: '',
    estimatedEffort: 3,
    projectId: 'project-1',
    authorId: 'user-1',
    assigneeId: 'user-2',
  };

  function createRoute(id: string | null): ActivatedRoute {
    return {
      paramMap: {
        subscribe: (observer: any) => {
          const paramMap = { get: (key: string) => id };
          if (observer.next) {
            observer.next(paramMap);
          }
          return { unsubscribe: () => { } };
        },
      },
    } as unknown as ActivatedRoute;
  }

  beforeEach(async () => {
    workItemService = {
      getWorkItemById: vi.fn(() => of(task)),
      updateWorkItemStatus: vi.fn(() => of(undefined)),
      assignWorkItem: vi.fn(() => of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [TaskDetailComponent],
      providers: [
        { provide: ActivatedRoute, useValue: createRoute('task-1') },
        { provide: WorkItemService, useValue: workItemService },
        // shallow stubs for other injected services
        { provide: ProjectService, useValue: { getProjectById: vi.fn(() => of(null)) } },
        {
          provide: UserService,
          useValue: {
            getUserById: vi.fn(() => of({ id: 'user-1', userName: 'current', emailAddress: 'current@example.com' })),
            getCurrentUserProfile: vi.fn(() => of({ id: 'user-1', userName: 'current', emailAddress: 'current@example.com' })),
          },
        },
        { provide: ProjectMemberService, useValue: { getProjectMembers: vi.fn(() => of([])) } },
        { provide: CommentService, useValue: { getComments: vi.fn(() => of([])) } },
        { provide: AttachmentService, useValue: { getAttachments: vi.fn(() => of([])) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskDetailComponent);
    component = fixture.componentInstance;
  });

  it('reads the task id from route params and loads task details', () => {
    component.ngOnInit();

    expect(workItemService.getWorkItemById).toHaveBeenCalledWith('task-1');
    expect(component.task).toEqual(task);
    expect(component.status).toBe('Active');
    expect(component.assigneeId).toBe('user-2');
  });

  it('does not load a task when the route has no id', async () => {
    // Recreate with null route
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [TaskDetailComponent],
      providers: [
        { provide: ActivatedRoute, useValue: createRoute(null) },
        { provide: WorkItemService, useValue: workItemService },
        { provide: ProjectService, useValue: { getProjectById: vi.fn(() => of(null)) } },
        {
          provide: UserService,
          useValue: {
            getUserById: vi.fn(() => of({ id: 'user-1', userName: 'current', emailAddress: 'current@example.com' })),
            getCurrentUserProfile: vi.fn(() => of({ id: 'user-1', userName: 'current', emailAddress: 'current@example.com' })),
          },
        },
        { provide: ProjectMemberService, useValue: { getProjectMembers: vi.fn(() => of([])) } },
        { provide: CommentService, useValue: { getComments: vi.fn(() => of([])) } },
        { provide: AttachmentService, useValue: { getAttachments: vi.fn(() => of([])) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskDetailComponent);
    component = fixture.componentInstance;
    component.loadTask();

    expect(workItemService.getWorkItemById).not.toHaveBeenCalled();
  });

  it('updates status and reloads the task', () => {
    fixture.detectChanges(); // trigger ngOnInit
    component.status = 'Resolved';

    component.updateStatus();

    expect(workItemService.updateWorkItemStatus).toHaveBeenCalledWith('task-1', {
      status: 'Resolved',
    });
  });

  it('assigns a user and reloads the task', () => {
    fixture.detectChanges(); // trigger ngOnInit
    component.assigneeId = 'user-3';

    component.assignUser();

    expect(workItemService.assignWorkItem).toHaveBeenCalledWith('task-1', {
      assigneeId: 'user-3',
    });
  });

  it('maps statuses to tag severities', () => {
    expect(component.getSeverity('New')).toBe('info');
    expect(component.getSeverity('Active')).toBe('warn');
    expect(component.getSeverity('Resolved')).toBe('success');
    expect(component.getSeverity('Closed')).toBe('secondary');
    expect(component.getSeverity('Unexpected')).toBe('info');
  });

  it('allows editing for task author', () => {
    component.task = { ...task, authorId: 'user-1' };
    component.currentUserId = 'user-1';

    expect(component.canEditTask()).toBe(true);
  });

  it('allows editing for project admin', () => {
    component.task = { ...task, authorId: 'author-1' };
    component.currentUserId = 'admin-1';
    component.projectUsers = [{ id: 'admin-1', fullName: 'Admin User', role: 'ADMIN' }];

    expect(component.canEditTask()).toBe(true);
  });

  it('blocks editing for regular project member', () => {
    component.task = { ...task, authorId: 'author-1' };
    component.currentUserId = 'member-1';
    component.projectUsers = [{ id: 'member-1', fullName: 'Member User', role: 'MEMBER' }];

    expect(component.canEditTask()).toBe(false);
  });
});
