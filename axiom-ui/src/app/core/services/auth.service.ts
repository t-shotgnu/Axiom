import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';

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
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  private tokenKey = 'axiom_jwt_token';
  
  private authStatus = new BehaviorSubject<boolean>(this.hasToken());
  public authStatus$ = this.authStatus.asObservable();

  constructor(private http: HttpClient) {}

  login(command: LoginCommand): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, command).pipe(
      tap(res => this.setToken(res.token))
    );
  }

  register(command: RegisterUserCommand): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, command).pipe(
      tap(res => this.setToken(res.token))
    );
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
