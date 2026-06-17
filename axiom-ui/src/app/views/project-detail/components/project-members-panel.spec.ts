import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { ProjectMember, ProjectMemberService } from '../../../core/services/project-member.service';
import { User, UserService } from '../../../core/services/user.service';
import { ProjectMembersPanelComponent } from './project-members-panel';

describe('ProjectMembersPanelComponent', () => {
  let fixture: ComponentFixture<ProjectMembersPanelComponent>;
  let component: ProjectMembersPanelComponent;
  let memberService: {
    getProjectMembers: ReturnType<typeof vi.fn>;
    addProjectMember: ReturnType<typeof vi.fn>;
    changeProjectMemberRole: ReturnType<typeof vi.fn>;
    removeProjectMember: ReturnType<typeof vi.fn>;
  };
  let userService: {
    getAllUsers: ReturnType<typeof vi.fn>;
    getCurrentUserProfile: ReturnType<typeof vi.fn>;
  };
  let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

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
      role: 'MEMBER',
      createdOn: '2026-01-01T12:00:00',
    },
  ];

  const users: User[] = [
    { id: 'user-1', userName: 'ada', emailAddress: 'ada@example.com', firstName: 'Ada', lastName: 'Lovelace' },
    { id: 'user-2', userName: 'grace', emailAddress: 'grace@example.com' },
    { id: 'user-3', userName: '', emailAddress: 'linus@example.com' },
  ];

  beforeEach(async () => {
    memberService = {
      getProjectMembers: vi.fn(() => of(members)),
      addProjectMember: vi.fn(() => of(void 0)),
      changeProjectMemberRole: vi.fn(() => of(void 0)),
      removeProjectMember: vi.fn(() => of(void 0)),
    };
    userService = {
      getAllUsers: vi.fn(() => of(users)),
      getCurrentUserProfile: vi.fn(() => of(users[0])),
    };
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    await TestBed.configureTestingModule({
      imports: [ProjectMembersPanelComponent],
      providers: [
        { provide: ProjectMemberService, useValue: memberService },
        { provide: UserService, useValue: userService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectMembersPanelComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    consoleErrorSpy.mockRestore();
  });

  function initialize(projectId = 'project-1'): void {
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();
  }

  it('loads members, users, current user, and emits manage permission', () => {
    const emitted: boolean[] = [];
    component.canManageProjectChange.subscribe((value) => emitted.push(value));

    initialize();

    expect(memberService.getProjectMembers).toHaveBeenCalledWith('project-1');
    expect(userService.getAllUsers).toHaveBeenCalled();
    expect(userService.getCurrentUserProfile).toHaveBeenCalled();
    expect(component.members()).toEqual(members);
    expect(component.users()).toEqual(users);
    expect(component.currentUserId()).toBe('user-1');
    expect(component.loadingMembers()).toBe(false);
    expect(component.loadingUsers()).toBe(false);
    expect(component.loadingCurrentUser()).toBe(false);
    expect(component.canManageProject()).toBe(true);
    expect(emitted.at(-1)).toBe(true);
  });

  it('filters already-added users out of add-member options', () => {
    initialize();

    expect(component.availableUsers).toEqual([users[2]]);
    expect(component.availableUserOptions).toEqual([
      { label: 'Select a user', value: '' },
      { label: 'linus@example.com', value: 'user-3' },
    ]);
  });

  it('formats member and user labels with sensible fallbacks', () => {
    initialize();

    expect(component.getMemberLabel(members[0])).toBe('Ada Lovelace');
    expect(component.getMemberLabel(members[1])).toBe('grace');
    expect(component.getUserLabel(users[0])).toBe('Ada Lovelace');
    expect(component.getUserLabel(users[2])).toBe('linus@example.com');
  });

  it('calculates current user, project lead, and remove permissions', () => {
    initialize();
    component.leadUserId = 'user-2';

    expect(component.isCurrentUser(members[0])).toBe(true);
    expect(component.isProjectLead(members[1])).toBe(true);
    expect(component.canRemoveMember(members[0])).toBe(false);
    expect(component.canRemoveMember(members[1])).toBe(false);

    const removableMember = { ...members[1], userId: 'user-3' };
    expect(component.canRemoveMember(removableMember)).toBe(true);
  });

  it('reloads data when project id changes after initialization', () => {
    initialize('project-1');
    memberService.getProjectMembers.mockClear();
    userService.getAllUsers.mockClear();

    fixture.componentRef.setInput('projectId', 'project-2');

    expect(memberService.getProjectMembers).toHaveBeenCalledWith('project-2');
    expect(userService.getAllUsers).toHaveBeenCalled();
  });

  it('adds a selected member, resets the form, and reloads members', () => {
    initialize();
    memberService.getProjectMembers.mockClear();
    component.selectedMemberUserId.set('user-3');
    component.selectedMemberRole.set('ADMIN');

    component.addMember();

    expect(memberService.addProjectMember).toHaveBeenCalledWith('project-1', {
      userId: 'user-3',
      role: 'ADMIN',
    });
    expect(component.selectedMemberUserId()).toBe('');
    expect(component.selectedMemberRole()).toBe('MEMBER');
    expect(memberService.getProjectMembers).toHaveBeenCalledWith('project-1');
  });

  it('does not add a member without a project or selected user', () => {
    component.selectedMemberUserId.set('user-3');
    component.addMember();

    component.projectId = 'project-1';
    component.selectedMemberUserId.set('');
    component.addMember();

    expect(memberService.addProjectMember).not.toHaveBeenCalled();
  });

  it('shows an add-member error when the request fails', () => {
    initialize();
    memberService.addProjectMember.mockReturnValue(throwError(() => new Error('nope')));
    component.selectedMemberUserId.set('user-3');

    component.addMember();

    expect(component.memberActionError()).toBe('Could not add the user to the project.');
  });

  it('changes member roles and maps role-change errors', () => {
    initialize();
    memberService.getProjectMembers.mockClear();

    component.changeMemberRole(members[1], 'ADMIN');

    expect(memberService.changeProjectMemberRole).toHaveBeenCalledWith('project-1', 'user-2', {
      role: 'ADMIN',
    });
    expect(memberService.getProjectMembers).toHaveBeenCalledWith('project-1');

    memberService.changeProjectMemberRole.mockReturnValueOnce(throwError(() => new Error('nope')));
    component.changeMemberRole(members[1], 'MEMBER');

    expect(component.memberActionError()).toBe('Could not change the member role.');
  });

  it('blocks self-removal and removes other members', () => {
    initialize();
    memberService.getProjectMembers.mockClear();

    component.removeMember(members[0]);

    expect(component.memberActionError()).toBe('You cannot remove yourself from the project.');
    expect(memberService.removeProjectMember).not.toHaveBeenCalled();

    component.removeMember(members[1]);

    expect(memberService.removeProjectMember).toHaveBeenCalledWith('project-1', 'user-2');
    expect(memberService.getProjectMembers).toHaveBeenCalledWith('project-1');
  });

  it('maps remove-member errors', () => {
    initialize();
    memberService.removeProjectMember.mockReturnValue(throwError(() => new Error('nope')));

    component.removeMember(members[1]);

    expect(component.memberActionError()).toBe('Could not remove the member from the project.');
  });

  it('stops loading after member and user load errors', () => {
    memberService.getProjectMembers.mockReturnValue(throwError(() => new Error('members failed')));
    userService.getAllUsers.mockReturnValue(throwError(() => new Error('users failed')));
    userService.getCurrentUserProfile.mockReturnValue(throwError(() => new Error('current user failed')));

    initialize();

    expect(component.loadingMembers()).toBe(false);
    expect(component.loadingUsers()).toBe(false);
    expect(component.loadingCurrentUser()).toBe(false);
    expect(consoleErrorSpy).toHaveBeenCalledTimes(3);
  });

  it('keeps loading state true until pending requests finalize', () => {
    const members$ = new Subject<ProjectMember[]>();
    const users$ = new Subject<User[]>();
    const currentUser$ = new Subject<User>();
    memberService.getProjectMembers.mockReturnValue(members$.asObservable());
    userService.getAllUsers.mockReturnValue(users$.asObservable());
    userService.getCurrentUserProfile.mockReturnValue(currentUser$.asObservable());

    initialize();

    expect(component.loadingMembers()).toBe(true);
    expect(component.loadingUsers()).toBe(true);
    expect(component.loadingCurrentUser()).toBe(true);

    members$.next(members);
    members$.complete();
    users$.next(users);
    users$.complete();
    currentUser$.next(users[0]);
    currentUser$.complete();

    expect(component.loadingMembers()).toBe(false);
    expect(component.loadingUsers()).toBe(false);
    expect(component.loadingCurrentUser()).toBe(false);
  });
});
