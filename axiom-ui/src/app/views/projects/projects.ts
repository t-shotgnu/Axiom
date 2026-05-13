import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { finalize } from 'rxjs';
import { CreateProjectCommand, Project, ProjectService } from '../../core/services/project.service';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ButtonModule, CardModule, InputTextModule],
  templateUrl: './projects.html',
})
export class ProjectsComponent {
  projects: Project[] = [];
  loading = false;
  creating = false;
  errorMessage = '';
  createErrorMessage = '';

  form = {
    name: '',
    code: '',
    description: '',
  };

  constructor(private readonly projectService: ProjectService) {
    this.loadProjects();
  }

  get listState(): 'loading' | 'error' | 'empty' | 'list' {
    if (this.loading) {
      return 'loading';
    }
    if (this.errorMessage) {
      return 'error';
    }
    if (this.projects.length === 0) {
      return 'empty';
    }
    return 'list';
  }

  loadProjects(): void {
    this.loading = true;
    this.errorMessage = '';

    this.projectService
      .getAllProjects()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (projects) => {
          this.projects = projects;
        },
        error: (err: unknown) => {
          this.errorMessage = this.mapProjectLoadError(err);
        },
      });
  }

  createProject(): void {
    this.creating = true;
    this.createErrorMessage = '';

    this.projectService
      .createProject(this.toCommand())
      .pipe(finalize(() => (this.creating = false)))
      .subscribe({
        next: () => {
          this.resetForm();
          this.loadProjects();
        },
        error: (err: unknown) => {
          this.createErrorMessage = this.mapProjectCreateError(err);
        },
      });
  }

  private toCommand(): CreateProjectCommand {
    const description = this.form.description.trim();

    return {
      name: this.form.name.trim(),
      code: this.form.code.trim().toUpperCase(),
      ...(description ? { description } : {}),
    };
  }

  private resetForm(): void {
    this.form = {
      name: '',
      code: '',
      description: '',
    };
  }

  private mapProjectLoadError(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401 || err.status === 403) {
        return 'You need to sign in to load projects. Sign in, then refresh this page.';
      }
    }
    return 'Could not load projects. Check if the API is running.';
  }

  private mapProjectCreateError(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401 || err.status === 403) {
        return 'Your session expired or you are not allowed to create projects. Sign in again, then retry.';
      }
    }
    return 'Could not create project. Check name and code.';
  }
}
