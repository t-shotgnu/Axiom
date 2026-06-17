import { ProjectDropdownComponent } from './project-dropdown';

describe('ProjectDropdownComponent', () => {
  const projects = [
    { id: 'project-1', name: 'Axiom', code: 'AX', type: 'Organization' },
    { id: 'project-2', name: 'Backend', code: 'BE', type: 'Engineering' },
    { id: 'project-3', name: 'Design', code: 'UX', type: 'Product' },
  ];

  function createComponent(): ProjectDropdownComponent {
    const component = new ProjectDropdownComponent();
    component.projects = projects;
    component.ngOnInit();
    return component;
  }

  it('initializes with all projects visible', () => {
    const component = createComponent();

    expect((component as any).filteredProjects).toEqual(projects);
  });

  it('opens with a reset search and full list', () => {
    const component = createComponent();
    (component as any).searchText = 'stale';
    (component as any).filteredProjects = [];

    component.toggleDropdown();

    expect((component as any).isOpen).toBe(true);
    expect((component as any).searchText).toBe('');
    expect((component as any).filteredProjects).toEqual(projects);
  });

  it('filters projects by name, code, or type', () => {
    const component = createComponent();

    (component as any).searchText = 'eng';
    component.onSearchChange();
    expect((component as any).filteredProjects).toEqual([projects[1]]);

    (component as any).searchText = 'ux';
    component.onSearchChange();
    expect((component as any).filteredProjects).toEqual([projects[2]]);

    (component as any).searchText = 'axiom';
    component.onSearchChange();
    expect((component as any).filteredProjects).toEqual([projects[0]]);
  });

  it('emits the selected project and closes', () => {
    const component = createComponent();
    const selected: unknown[] = [];
    component.projectSelected.subscribe((project) => selected.push(project));
    component.toggleDropdown();

    component.selectProject(projects[1]);

    expect(component.activeProject).toEqual(projects[1]);
    expect(selected).toEqual([projects[1]]);
    expect((component as any).isOpen).toBe(false);
  });

  it('emits create project and closes', () => {
    const component = createComponent();
    const emitted: unknown[] = [];
    component.createProject.subscribe((value) => emitted.push(value));
    component.toggleDropdown();

    component.triggerCreateProject();

    expect(emitted).toEqual([undefined]);
    expect((component as any).isOpen).toBe(false);
  });
});
