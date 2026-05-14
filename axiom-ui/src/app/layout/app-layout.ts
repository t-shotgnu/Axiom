import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { Subscription, filter } from 'rxjs';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, ButtonModule],
  templateUrl: './app-layout.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppLayoutComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private subs = new Subscription();

  /** Cached — avoid decoding JWT on every change detection tick (was dominating render cost). */
  protected isAuthenticated = false;
  protected userEmail = '';

  ngOnInit(): void {
    this.refreshSessionBanner();
    this.subs.add(
      this.authService.authStatus$.subscribe(() => this.refreshSessionBanner()),
    );
    this.subs.add(
      this.router.events
        .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
        .subscribe(() => this.refreshSessionBanner()),
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  logout(): void {
    this.authService.logout();
    this.refreshSessionBanner();
    void this.router.navigateByUrl('/login');
  }

  private refreshSessionBanner(): void {
    const token = this.authService.getToken();
    this.isAuthenticated = !!token;
    this.userEmail = token ? this.decodeEmailFromJwt(token) : '';
    this.cdr.markForCheck();
  }

  private decodeEmailFromJwt(token: string): string {
    try {
      const payload = JSON.parse(atob(token.split('.')[1] as string));
      return (payload.sub as string) ?? '';
    } catch {
      return '';
    }
  }
}
