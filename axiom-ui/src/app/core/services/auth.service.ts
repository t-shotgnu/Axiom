import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface LoginCommand {
  emailAddress: string;
  password: string;
}

export interface RegisterUserCommand {
  userName: string;
  emailAddress: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = '/api/auth';
  private readonly tokenKey = 'axiom_jwt_token';

  private readonly authStatus = new BehaviorSubject<boolean>(this.hasToken());
  readonly authStatus$ = this.authStatus.asObservable();

  constructor(private readonly http: HttpClient) {}

  login(command: LoginCommand): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, command)
      .pipe(tap((response) => this.setToken(response.token)));
  }

  register(command: RegisterUserCommand): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, command)
      .pipe(tap((response) => this.setToken(response.token)));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.authStatus.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  hasToken(): boolean {
    return !!this.getToken();
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
    this.authStatus.next(true);
  }
}
