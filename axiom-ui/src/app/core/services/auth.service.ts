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
  firstName: string;
  lastName: string;
  dateOfBirth: string;
}

export interface RefreshTokenCommand {
  refreshToken: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = '/api/auth';
  private readonly tokenKey = 'axiom_jwt_token';
  private readonly refreshTokenKey = 'axiom_refresh_token';

  private readonly authStatus = new BehaviorSubject<boolean>(this.hasToken());
  readonly authStatus$ = this.authStatus.asObservable();

  constructor(private readonly http: HttpClient) { }

  login(command: LoginCommand): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, command)
      .pipe(tap((response) => this.setTokens(response)));
  }

  register(command: RegisterUserCommand): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, command)
      .pipe(tap((response) => this.setTokens(response)));
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    return this.http
      .post<AuthResponse>(`${this.apiUrl}/refresh-token`, { refreshToken } satisfies RefreshTokenCommand)
      .pipe(tap((response) => this.setTokens(response)));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    this.authStatus.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  hasToken(): boolean {
    return !!this.getToken();
  }

  private setTokens(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.token);
    localStorage.setItem(this.refreshTokenKey, response.refreshToken);
    this.authStatus.next(true);
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
    this.authStatus.next(true);
  }
}
