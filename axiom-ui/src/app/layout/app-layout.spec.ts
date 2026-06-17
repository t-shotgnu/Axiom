import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { signal } from '@angular/core';
import { AuthService } from '../core/services/auth.service';
import { Project, ProjectService } from '../core/services/project.service';
import { ThemeService } from '../core/services/theme.service';
import { AppLayoutComponent } from './app-layout';

describe('AppLayoutComponent', () => {
  let fixture: ComponentFixture<AppLayoutComponent>;
  let component: AppLayoutComponent;
  let authStatus$: Subject<boolean>;
  let routerEvents$: Subject<NavigationEnd>;
  let authService: {
    authStatus$: Subject<boolean>;
    getToken: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
  };
  let projectService: {
    getAllProjects: ReturnType<typeof vi.fn>;
    setCurrentProject: ReturnType<typeof vi.fn>;
  };
  let router: {
    events: Subject<NavigationEnd>;
    navigateByUrl: ReturnType<typeof vi.fn>;
  };
  let themeService: {
    currentTheme: ReturnType<typeof signal<'light' | 'dark' | 'system'>>;
    setTheme: ReturnType<typeof vi.fn>;
  };

  const projects: Project[] = [
    {
      id: 'project-1',
      name: 'Axiom',
      code: 'ax',
      description: 'Planning',
      createdOn: '2026-01-01T12:00:00',
      ownerId: 'user-1',
      ownerName: 'Ada',
    },
    {
      id: 'project-2',
      name: 'Backend',
      code: '',
      description: 'API',
      createdOn: '2026-01-02T12:00:00',
      ownerId: 'user-2',
    },
  ];

  beforeEach(async () => {
    authStatus$ = new Subject<boolean>();
    routerEvents$ = new Subject<NavigationEnd>();
    authService = {
      authStatus$,
      getToken: vi.fn(() => tokenForSubject('ada@example.com')),
      logout: vi.fn(),
    };
    projectService = {
      getAllProjects: vi.fn(() => of(projects)),
      setCurrentProject: vi.fn(),
    };
    router = {
      events: routerEvents$,
      navigateByUrl: vi.fn(() => Promise.resolve(true)),
    };
    const currentTheme = signal<'light' | 'dark' | 'system'>('system');
    themeService = {
      currentTheme,
      setTheme: vi.fn((theme: 'light' | 'dark' | 'system') => currentTheme.set(theme)),
    };

    await TestBed.configureTestingModule({
      imports: [AppLayoutComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: ProjectService, useValue: projectService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: { snapshot: {}, queryParams: of({}), params: of({}) } },
        { provide: ThemeService, useValue: themeService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AppLayoutComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    fixture?.destroy();
  });

  it('loads the session banner and project dropdown on init', () => {
    component.ngOnInit();

    expect((component as any).isAuthenticated).toBe(true);
    expect((component as any).userEmail).toBe('ada@example.com');
    expect((component as any).dropdownProjects).toEqual([
      {
        id: 'project-1',
        name: 'Axiom',
        code: 'AX',
        type: 'Software Project',
        colorClass: 'bg-[#0052cc]',
      },
      {
        id: 'project-2',
        name: 'Backend',
        code: 'BA',
        type: 'Software Project',
        colorClass: 'bg-[#0747a6]',
      },
    ]);
    expect((component as any).activeProject.id).toBe('project-1');
    expect(projectService.setCurrentProject).toHaveBeenCalledWith(projects[0]);
  });

  it('clears the active project when no projects are returned', () => {
    projectService.getAllProjects.mockReturnValue(of([]));

    component.ngOnInit();

    expect((component as any).dropdownProjects).toEqual([]);
    expect((component as any).activeProject).toEqual({
      id: '',
      name: 'No projects',
      code: 'AX',
      type: 'Organization',
      colorClass: 'bg-primary',
    });
    expect(projectService.setCurrentProject).toHaveBeenCalledWith(null);
  });

  it('shows an error project placeholder when project loading fails', () => {
    projectService.getAllProjects.mockReturnValue(throwError(() => new Error('failed')));

    component.ngOnInit();

    expect((component as any).dropdownProjects).toEqual([]);
    expect((component as any).activeProject).toEqual({
      id: '',
      name: 'Error loading projects',
      code: 'ER',
      type: 'Organization',
      colorClass: 'bg-red-600',
    });
    expect(projectService.setCurrentProject).toHaveBeenCalledWith(null);
  });

  it('selects a project, resolves the full project object, and navigates to projects for creation', () => {
    const dropdownProject = {
      id: 'project-2',
      name: 'Backend',
      code: 'BE',
      type: 'Software Project',
      colorClass: 'bg-[#0747a6]',
    };

    component.selectProject(dropdownProject);
    component.createProjectFromDropdown();

    expect((component as any).activeProject).toEqual(dropdownProject);
    expect(projectService.setCurrentProject).toHaveBeenCalledWith(projects[1]);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/projects');
  });

  it('toggles menus, closes them on document click, and updates the selected theme', () => {
    const event = { stopPropagation: vi.fn() } as unknown as Event;

    component.toggleUserMenu(event);
    component.toggleThemeDropdown(event);

    expect(event.stopPropagation).toHaveBeenCalledTimes(2);
    expect((component as any).showUserMenu).toBe(true);
    expect((component as any).showThemeDropdown).toBe(true);

    component.setTheme('dark');

    expect(themeService.setTheme).toHaveBeenCalledWith('dark');
    expect((component as any).showThemeDropdown).toBe(false);

    component.toggleUserMenu(event);
    component.toggleThemeDropdown(event);
    component.onDocumentClick();

    expect((component as any).showUserMenu).toBe(false);
    expect((component as any).showThemeDropdown).toBe(false);
  });

  it('opens account dialogs, refreshes profile data after save, and logs out', () => {
    component.ngOnInit();
    projectService.getAllProjects.mockClear();

    component.openEditProfileDialog();
    component.openChangePasswordDialog();
    component.onProfileSaved();
    component.logout();

    expect((component as any).showEditProfileDialog).toBe(true);
    expect((component as any).showChangePasswordDialog).toBe(true);
    expect(projectService.getAllProjects).not.toHaveBeenCalled();
    expect(authService.logout).toHaveBeenCalled();
    expect((component as any).isAuthenticated).toBe(true);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });

  it('reacts to auth status and navigation events', () => {
    component.ngOnInit();
    projectService.getAllProjects.mockClear();
    authService.getToken.mockReturnValue(null);

    authStatus$.next(false);
    routerEvents$.next(new NavigationEnd(1, '/tasks', '/tasks'));

    expect((component as any).isAuthenticated).toBe(false);
    expect((component as any).userEmail).toBe('');
    expect(projectService.getAllProjects).toHaveBeenCalledTimes(1);
  });

  it('falls back to a blank email for malformed tokens', () => {
    authService.getToken.mockReturnValue('not-a-jwt');

    component.ngOnInit();

    expect((component as any).isAuthenticated).toBe(true);
    expect((component as any).userEmail).toBe('');
  });

  function tokenForSubject(subject: string): string {
    return `header.${globalThis.btoa(JSON.stringify({ sub: subject }))}.signature`;
  }
});
