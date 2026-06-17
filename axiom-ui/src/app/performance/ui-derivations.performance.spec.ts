import { ProjectsComponent } from '../views/projects/projects';
import { ProjectDropdownComponent } from '../shared/components/ui/project-dropdown';

describe('UI derivation performance smoke tests', () => {
  it('paginates a large project collection without mutating the source list', () => {
    const component = Object.create(ProjectsComponent.prototype) as ProjectsComponent;
    const projects = Array.from({ length: 5000 }, (_, index) => ({
      id: `project-${index}`,
      name: `Project ${index}`,
      code: `P${index}`,
      description: 'A sizeable project list',
      createdOn: '2026-01-01',
      ownerId: 'user-1',
    }));

    (component as any).projects = projects;
    (component as any).pageSize = 8;
    (component as any).currentPage = 250;
    (component as any).searchTerm = '';

    const start = performance.now();
    const pagedProjects = component.pagedProjects;
    const durationMs = performance.now() - start;

    expect(pagedProjects).toHaveLength(8);
    expect(pagedProjects[0].id).toBe('project-1992');
    expect(projects).toHaveLength(5000);
    expect(durationMs).toBeLessThan(25);
  });

  it('filters a large project dropdown quickly and case-insensitively', () => {
    const component = new ProjectDropdownComponent();
    component.projects = Array.from({ length: 3000 }, (_, index) => ({
      id: `project-${index}`,
      name: index === 2750 ? 'Observability Platform' : `Project ${index}`,
      code: index === 2750 ? 'OBS' : `P${index}`,
      type: index === 2750 ? 'Operations' : 'Delivery',
    }));
    component.ngOnInit();
    (component as any).searchText = 'observability';

    const start = performance.now();
    component.onSearchChange();
    const durationMs = performance.now() - start;

    expect((component as any).filteredProjects).toEqual([
      {
        id: 'project-2750',
        name: 'Observability Platform',
        code: 'OBS',
        type: 'Operations',
      },
    ]);
    expect(durationMs).toBeLessThan(30);
  });
});
