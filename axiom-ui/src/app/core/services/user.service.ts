import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id: string;
  userName: string;
  emailAddress: string;
  firstName?: string;
  lastName?: string;
  dateOfBirth?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/users';

  getUserById(id: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  getCurrentUserProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }

  updateCurrentUserProfile(command: { firstName: string; lastName: string; dateOfBirth: string }): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/me`, command);
  }
}
