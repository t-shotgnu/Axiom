import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { finalize } from 'rxjs';
import { Project, ProjectService } from '../../core/services/project.service';
import { CreateProjectComponent } from './create-project';
import { DialogComponent } from '../../shared/components/ui/dialog';
import { ButtonComponent } from '../../shared/components/ui/button';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, RouterModule, CreateProjectComponent, DialogComponent, ButtonComponent],
  templateUrl: './projects.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectsComponent {
  projects: Project[] = [];
  loading = false;
  errorMessage = '';
  deleteDialogVisible = false;
  deleteDialogProject: Project | null = null;
  deleting = false;
  deleteErrorMessage = '';

  listState: 'loading' | 'error' | 'empty' | 'list' = 'loading';

  constructor(
    private readonly projectService: ProjectService,
    private readonly cdr: ChangeDetectorRef,
  ) {
    this.loadProjects();
  }

  private syncListState(): void {
    if (this.loading) {
      this.listState = 'loading';
    } else if (this.errorMessage) {
      this.listState = 'error';
    } else if (this.projects.length === 0) {
      this.listState = 'empty';
    } else {
      this.listState = 'list';
    }
    this.cdr.markForCheck();
  }

  loadProjects(): void {
    this.loading = true;
    this.errorMessage = '';
    this.syncListState();

    this.projectService
      .getAllProjects()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.syncListState();
        }),
      )
      .subscribe({
        next: (projects) => {
          this.projects = projects;
        },
        error: (err: unknown) => {
          this.errorMessage = this.mapProjectLoadError(err);
        },
      });
  }

  openDeleteDialog(project: Project): void {
    this.deleteDialogProject = project;
    this.deleteErrorMessage = '';
    this.deleteDialogVisible = true;
    this.cdr.markForCheck();
  }

  cancelDeleteDialog(): void {
    this.deleteDialogVisible = false;
    this.deleteDialogProject = null;
    this.deleteErrorMessage = '';
    this.cdr.markForCheck();
  }

  confirmDeleteProject(): void {
    if (!this.deleteDialogProject || this.deleting) {
      return;
    }

    this.deleting = true;
    this.deleteErrorMessage = '';

    this.projectService
      .deleteProject(this.deleteDialogProject.id)
      .pipe(
        finalize(() => {
          this.deleting = false;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: () => {
          this.cancelDeleteDialog();
          this.loadProjects();
        },
        error: (err: unknown) => {
          this.deleteErrorMessage = this.mapProjectDeleteError(err);
          this.cdr.markForCheck();
        },
      });
  }

  private mapProjectLoadError(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401 || err.status === 403) {
        return 'You need to sign in to load projects. Sign in, then refresh this page.';
      }
      if (err.status === 0) {
        return 'Could not reach the API. Check your network or that the backend is running.';
      }
      const apiMessage = this.extractApiMessage(err);
      if (apiMessage) {
        return apiMessage;
      }
    }
    return 'Could not load projects. Check if the API is running.';
  }

  private mapProjectDeleteError(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401) {
        return 'Your session expired. Sign in again, then retry deleting the project.';
      }
      if (err.status === 403) {
        return 'You are not allowed to delete this project.';
      }
      if (err.status === 0) {
        return 'Could not reach the API. Check your network connection.';
      }
      const apiMessage = this.extractApiMessage(err);
      if (apiMessage) {
        return apiMessage;
      }
    }
    return 'Could not delete the project. Please try again.';
  }

  private extractApiMessage(err: HttpErrorResponse): string | null {
    const body = err.error;
    if (body && typeof body === 'object') {
      if ('detail' in body) {
        const detail = (body as { detail?: unknown }).detail;
        if (typeof detail === 'string' && detail.trim() !== '') {
          return detail;
        }
      }
      if ('message' in body) {
        const message = (body as { message?: unknown }).message;
        if (typeof message === 'string' && message.trim() !== '') {
          return message;
        }
      }
    }
    if (typeof body === 'string' && body.trim() !== '') {
      return body;
    }
    return null;
  }
}
