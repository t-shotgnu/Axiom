import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProjectService } from './project.service';

describe('ProjectService', () => {
  let service: ProjectService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ProjectService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads all projects', () => {
    service.getAllProjects().subscribe((projects) => {
      expect(projects).toEqual([
        {
          id: 'project-1',
          name: 'Axiom',
          code: 'AX',
          description: 'Main project',
          createdOn: '2026-01-01',
          ownerId: 'user-1',
        },
      ]);
    });

    const request = httpMock.expectOne('/api/projects');
    expect(request.request.method).toBe('GET');
    request.flush([
      {
        id: 'project-1',
        name: 'Axiom',
        code: 'AX',
        description: 'Main project',
        createdOn: '2026-01-01',
        ownerId: 'user-1',
      },
    ]);
  });

  it('loads a project by id', () => {
    service.getProjectById('project-1').subscribe();

    const request = httpMock.expectOne('/api/projects/project-1');
    expect(request.request.method).toBe('GET');
    request.flush({
      id: 'project-1',
      name: 'Axiom',
      code: 'AX',
      description: '',
      createdOn: '2026-01-01',
      ownerId: 'user-1',
    });
  });

  it('creates a project', () => {
    const command = { name: 'Axiom', code: 'AX', description: 'Main project' };

    service.createProject(command).subscribe((id) => {
      expect(id).toBe('project-1');
    });

    const request = httpMock.expectOne('/api/projects');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(command);
    request.flush('project-1');
  });

  it('updates a project', () => {
    const command = { name: 'Renamed project', code: 'RP' };

    service.updateProject('project-1', command).subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/projects/project-1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(command);
    request.flush(null);
  });

  it('deletes a project', () => {
    service.deleteProject('project-1').subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/projects/project-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('publishes and returns the selected current project', () => {
    const project = {
      id: 'project-1',
      name: 'Axiom',
      code: 'AX',
      description: 'Main project',
      createdOn: '2026-01-01',
      ownerId: 'user-1',
    };
    const seenProjects: Array<typeof project | null> = [];
    service.currentProject$.subscribe((currentProject) => seenProjects.push(currentProject));

    service.setCurrentProject(project);
    expect(service.getCurrentProject()).toEqual(project);

    service.setCurrentProject(null);
    expect(service.getCurrentProject()).toBeNull();
    expect(seenProjects).toEqual([null, project, null]);
  });
});
