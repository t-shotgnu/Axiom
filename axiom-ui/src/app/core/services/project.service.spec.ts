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
});
