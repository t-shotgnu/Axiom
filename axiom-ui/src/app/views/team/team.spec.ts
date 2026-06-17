import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, Subject, throwError } from 'rxjs';
import { Project, ProjectService } from '../../core/services/project.service';
import { ProjectMember, ProjectMemberService } from '../../core/services/project-member.service';
import { TeamComponent } from './team';

describe('TeamComponent', () => {
  let fixture: ComponentFixture<TeamComponent>;
  let component: TeamComponent;
  let currentProject$: BehaviorSubject<Project | null>;
  let memberService: { getProjectMembers: ReturnType<typeof vi.fn> };
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

  const project: Project = {
    id: 'project-1',
    name: 'Axiom',
    code: 'AX',
    description: 'Main project',
    createdOn: '2026-01-01',
    ownerId: 'user-1',
  };

  const members: ProjectMember[] = [
    {
      userId: 'user-1',
      userName: 'ada',
      emailAddress: 'ada@example.com',
      firstName: 'Ada',
      lastName: 'Lovelace',
      role: 'ADMIN',
      createdOn: '2026-01-01T12:00:00',
    },
    {
      userId: 'user-2',
      userName: 'grace',
      emailAddress: 'grace@example.com',
      role: 'ADMIN',
      createdOn: '2026-01-01T12:00:00',
    },
    {
      userId: 'user-3',
      userName: '',
      emailAddress: 'linus@example.com',
      role: 'MEMBER',
      createdOn: '2026-01-01T12:00:00',
    },
  ];

  beforeEach(async () => {
    currentProject$ = new BehaviorSubject<Project | null>(null);
    memberService = {
      getProjectMembers: vi.fn(),
    };
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    await TestBed.configureTestingModule({
      imports: [TeamComponent],
      providers: [
        {
          provide: ProjectService,
          useValue: { currentProject$: currentProject$.asObservable() },
        },
        { provide: ProjectMemberService, useValue: memberService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TeamComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    consoleErrorSpy.mockRestore();
  });

  it('clears members and loading state when no project is selected', () => {
    fixture.detectChanges();

    expect(component.currentProject).toBeNull();
    expect(component.members).toEqual([]);
    expect(component.loading).toBe(false);
    expect(memberService.getProjectMembers).not.toHaveBeenCalled();
  });

  it('loads members when the selected project changes', () => {
    const request$ = new Subject<ProjectMember[]>();
    memberService.getProjectMembers.mockReturnValue(request$.asObservable());
    fixture.detectChanges();

    currentProject$.next(project);

    expect(component.currentProject).toEqual(project);
    expect(component.loading).toBe(true);
    expect(memberService.getProjectMembers).toHaveBeenCalledWith('project-1');

    request$.next(members);
    request$.complete();

    expect(component.members).toEqual(members);
    expect(component.loading).toBe(false);
  });

  it('clears members and stops loading after a member request fails', () => {
    memberService.getProjectMembers.mockReturnValue(throwError(() => new Error('boom')));
    fixture.detectChanges();

    currentProject$.next(project);

    expect(component.members).toEqual([]);
    expect(component.loading).toBe(false);
    expect(consoleErrorSpy).toHaveBeenCalledWith('Error loading team members', expect.any(Error));
  });

  it('formats display names, initials, labels, and role badge classes', () => {
    fixture.detectChanges();
    component.currentProject = project;

    expect(component.getMemberDisplayName(members[0])).toBe('Ada Lovelace');
    expect(component.getMemberDisplayName(members[1])).toBe('grace');
    expect(component.getMemberDisplayName(members[2])).toBe('linus@example.com');
    expect(component.getMemberInitial(members[0])).toBe('A');

    expect(component.isLead(members[0])).toBe(true);
    expect(component.getRoleLabel(members[0])).toBe('Lead');
    expect(component.getRoleBadgeClass(members[0])).toBe('bg-primary/10 text-primary');

    expect(component.getRoleLabel(members[1])).toBe('Admin');
    expect(component.getRoleBadgeClass(members[1])).toBe('bg-violet-500/10 text-violet-700');

    expect(component.getRoleLabel(members[2])).toBe('Member');
    expect(component.getRoleBadgeClass(members[2])).toBe('bg-surface-variant text-on-surface-variant');
  });

  it('unsubscribes from project changes on destroy', () => {
    memberService.getProjectMembers.mockReturnValue(new Subject<ProjectMember[]>().asObservable());
    fixture.detectChanges();

    fixture.destroy();
    currentProject$.next(project);

    expect(memberService.getProjectMembers).not.toHaveBeenCalled();
  });
});
