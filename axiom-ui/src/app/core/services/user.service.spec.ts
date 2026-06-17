import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads a user by id', () => {
    service.getUserById('user-1').subscribe((user) => {
      expect(user.emailAddress).toBe('ada@example.com');
    });

    const request = httpMock.expectOne('/api/users/user-1');
    expect(request.request.method).toBe('GET');
    request.flush({
      id: 'user-1',
      userName: 'ada',
      emailAddress: 'ada@example.com',
    });
  });

  it('loads all users', () => {
    service.getAllUsers().subscribe((users) => {
      expect(users).toHaveLength(2);
    });

    const request = httpMock.expectOne('/api/users');
    expect(request.request.method).toBe('GET');
    request.flush([
      { id: 'user-1', userName: 'ada', emailAddress: 'ada@example.com' },
      { id: 'user-2', userName: 'grace', emailAddress: 'grace@example.com' },
    ]);
  });

  it('loads the current user profile', () => {
    service.getCurrentUserProfile().subscribe((user) => {
      expect(user.firstName).toBe('Ada');
    });

    const request = httpMock.expectOne('/api/users/me');
    expect(request.request.method).toBe('GET');
    request.flush({
      id: 'user-1',
      userName: 'ada',
      emailAddress: 'ada@example.com',
      firstName: 'Ada',
      lastName: 'Lovelace',
      dateOfBirth: '1815-12-10',
    });
  });

  it('updates the current user profile', () => {
    const command = {
      firstName: 'Grace',
      lastName: 'Hopper',
      dateOfBirth: '1906-12-09',
    };

    service.updateCurrentUserProfile(command).subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/users/me');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(command);
    request.flush(null);
  });
});
