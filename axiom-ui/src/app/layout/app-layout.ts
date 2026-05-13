import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';
import { Router } from '@angular/router';
import { AuthApi } from '../services/auth-api';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, MenubarModule, ButtonModule],
  templateUrl: './app-layout.html',
  styles: [],
})
export class AppLayoutComponent {
  constructor(
    protected readonly authApi: AuthApi,
    private readonly router: Router,
  ) {}

  logout(): void {
    this.authApi.logout();
    void this.router.navigateByUrl('/login');
  }
}
