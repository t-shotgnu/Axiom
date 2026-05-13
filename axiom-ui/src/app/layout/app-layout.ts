import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MenubarModule } from 'primeng/menubar';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, MenubarModule, ButtonModule],
  templateUrl: './app-layout.html',
  styles: [],
})
export class AppLayoutComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  get isAuthenticated(): boolean {
    return this.authService.hasToken();
  }

  get userEmail(): string {
    const token = this.authService.getToken();
    if (!token) {
      return '';
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1] as string));
      return (payload.sub as string) ?? '';
    } catch {
      return '';
    }
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }
}
