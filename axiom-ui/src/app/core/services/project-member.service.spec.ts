import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProjectMemberService } from './project-member.service';

describe('ProjectMemberService', () => {
  let service: ProjectMemberService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ProjectMemberService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads members for a project', () => {
    service.getProjectMembers('project-1').subscribe((members) => {
      expect(members).toEqual([
        {
          userId: 'user-1',
          userName: 'ada',
          emailAddress: 'ada@example.com',
          firstName: 'Ada',
          lastName: 'Lovelace',
          role: 'ADMIN',
          createdOn: '2026-01-01T12:00:00',
        },
      ]);
    });

    const request = httpMock.expectOne('/api/projects/project-1/members');
    expect(request.request.method).toBe('GET');
    request.flush([
      {
        userId: 'user-1',
        userName: 'ada',
        emailAddress: 'ada@example.com',
        firstName: 'Ada',
        lastName: 'Lovelace',
        role: 'ADMIN',
        createdOn: '2026-01-01T12:00:00',
      },
    ]);
  });

  it('adds a project member with a role', () => {
    const command = { userId: 'user-2', role: 'MEMBER' as const };

    service.addProjectMember('project-1', command).subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/projects/project-1/members');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(command);
    request.flush(null);
  });

  it('changes a member role', () => {
    service
      .changeProjectMemberRole('project-1', 'user-2', { role: 'ADMIN' })
      .subscribe((result) => {
        expect(result).toBeNull();
      });

    const request = httpMock.expectOne('/api/projects/project-1/members/user-2');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ role: 'ADMIN' });
    request.flush(null);
  });

  it('removes a project member', () => {
    service.removeProjectMember('project-1', 'user-2').subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/projects/project-1/members/user-2');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
