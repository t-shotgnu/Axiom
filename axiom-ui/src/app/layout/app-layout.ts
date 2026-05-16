import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  HostListener,
  OnDestroy,
  OnInit,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription, filter } from 'rxjs';
import { AuthService } from '../core/services/auth.service';
import { ProjectService } from '../core/services/project.service';
import { ThemeService, Theme } from '../core/services/theme.service';
import { ProjectDropdownComponent, DropdownProject } from '../shared/components/ui/project-dropdown';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ProjectDropdownComponent],
  templateUrl: './app-layout.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppLayoutComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly projectService = inject(ProjectService);
  protected readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private subs = new Subscription();

  protected showThemeDropdown = false;

  protected activeProject: DropdownProject = {
    id: '',
    name: 'Loading projects...',
    code: 'AX',
    type: 'Organization',
    colorClass: 'bg-primary',
  };

  protected dropdownProjects: DropdownProject[] = [];

  selectProject(proj: DropdownProject): void {
    this.activeProject = proj;
    this.subs.add(
      this.projectService.getAllProjects().subscribe(projects => {
        const fullProj = projects.find(p => p.id === proj.id);
        if (fullProj) {
          this.projectService.setCurrentProject(fullProj);
        }
      })
    );
    this.cdr.markForCheck();
  }

  createProjectFromDropdown(): void {
    void this.router.navigateByUrl('/projects');
  }

  toggleThemeDropdown(event: Event): void {
    event.stopPropagation();
    this.showThemeDropdown = !this.showThemeDropdown;
  }

  setTheme(theme: Theme): void {
    this.themeService.setTheme(theme);
    this.showThemeDropdown = false;
    this.cdr.markForCheck();
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.showThemeDropdown = false;
  }

  /** Cached — avoid decoding JWT on every change detection tick (was dominating render cost). */
  protected isAuthenticated = false;
  protected userEmail = '';

  ngOnInit(): void {
    this.refreshSessionBanner();
    this.loadDropdownProjects();
    this.subs.add(
      this.authService.authStatus$.subscribe(() => this.refreshSessionBanner()),
    );
    this.subs.add(
      this.router.events
        .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
        .subscribe(() => {
          this.refreshSessionBanner();
          this.loadDropdownProjects();
        }),
    );
  }

  private loadDropdownProjects(): void {
    this.subs.add(
      this.projectService.getAllProjects().subscribe({
        next: (projects) => {
          if (projects && projects.length > 0) {
            const colors = [
              'bg-[#0052cc]',
              'bg-[#0747a6]',
              'bg-[#8b5cf6]',
              'bg-[#0ea5e9]',
              'bg-green-600',
              'bg-indigo-600',
            ];
            this.dropdownProjects = projects.map((p, idx) => ({
              id: p.id,
              name: p.name,
              code: p.code ? p.code.toUpperCase() : p.name.substring(0, 2).toUpperCase(),
              type: 'Software Project',
              colorClass: colors[idx % colors.length] || 'bg-primary',
            }));

            // Restore active project, or select first if not set
            const exists = this.dropdownProjects.find((p) => p.id === this.activeProject.id);
            if (exists) {
              this.activeProject = exists;
            } else {
              this.activeProject = this.dropdownProjects[0]!;
            }

            const fullProj = projects.find((p) => p.id === this.activeProject.id) || projects[0]!;
            this.projectService.setCurrentProject(fullProj);
          } else {
            this.dropdownProjects = [];
            this.activeProject = {
              id: '',
              name: 'No projects',
              code: 'AX',
              type: 'Organization',
              colorClass: 'bg-primary',
            };
            this.projectService.setCurrentProject(null);
          }
          this.cdr.markForCheck();
        },
        error: () => {
          this.dropdownProjects = [];
          this.activeProject = {
            id: '',
            name: 'Error loading projects',
            code: 'ER',
            type: 'Organization',
            colorClass: 'bg-red-600',
          };
          this.projectService.setCurrentProject(null);
          this.cdr.markForCheck();
        },
      })
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
