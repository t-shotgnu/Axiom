import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { finalize } from 'rxjs';
import { Project, ProjectService } from '../../core/services/project.service';
import { WorkItemService } from '../../core/services/work-item.service';
import { CreateProjectComponent } from './create-project';
import { DialogComponent } from '../../shared/components/ui/dialog';
import { ButtonComponent } from '../../shared/components/ui/button';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, CreateProjectComponent, DialogComponent, ButtonComponent],
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
  searchTerm = '';
  currentPage = 1;
  readonly pageSize = 8;

  listState: 'loading' | 'error' | 'empty' | 'list' = 'loading';

  projectStats = new Map<string, { total: number; completed: number; percentage: number }>();

  get filteredProjects(): Project[] {
    if (!this.searchTerm.trim()) {
      return this.projects;
    }
    const term = this.searchTerm.toLowerCase();
    return this.projects.filter(
      (p) =>
        p.name.toLowerCase().includes(term) ||
        p.code.toLowerCase().includes(term) ||
        (p.ownerName || '').toLowerCase().includes(term) ||
        (p.description || '').toLowerCase().includes(term),
    );
  }

  get totalPages(): number {
    return Math.max(Math.ceil(this.filteredProjects.length / this.pageSize), 1);
  }

  get pagedProjects(): Project[] {
    const page = Math.min(this.currentPage, this.totalPages);
    const start = (page - 1) * this.pageSize;
    return this.filteredProjects.slice(start, start + this.pageSize);
  }

  get firstVisibleProjectIndex(): number {
    if (this.filteredProjects.length === 0) {
      return 0;
    }
    return (Math.min(this.currentPage, this.totalPages) - 1) * this.pageSize + 1;
  }

  get lastVisibleProjectIndex(): number {
    return Math.min(Math.min(this.currentPage, this.totalPages) * this.pageSize, this.filteredProjects.length);
  }

  constructor(
    private readonly projectService: ProjectService,
    private readonly workItemService: WorkItemService,
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
          this.currentPage = 1;
          this.loadAllProjectStats();
        },
        error: (err: unknown) => {
          this.errorMessage = this.mapProjectLoadError(err);
        },
      });
  }

  onSearchTermChange(): void {
    this.currentPage = 1;
  }

  goToPage(page: number): void {
    this.currentPage = Math.min(Math.max(page, 1), this.totalPages);
  }

  getPagesArray(): number[] {
    if (this.totalPages <= 7) {
      return Array.from({ length: this.totalPages }, (_, index) => index + 1);
    }

    const pages = new Set<number>([1, this.totalPages]);
    for (let page = this.currentPage - 2; page <= this.currentPage + 2; page++) {
      if (page > 1 && page < this.totalPages) {
        pages.add(page);
      }
    }
    return Array.from(pages).sort((a, b) => a - b);
  }

  private loadAllProjectStats(): void {
    this.projectStats.clear();
    for (const project of this.projects) {
      this.workItemService.getWorkItems(project.id).subscribe({
        next: (workItems) => {
          const items = workItems || [];
          const total = items.length;
          const completed = items.filter(w => {
            const s = (w.status || '').toLowerCase();
            return s === 'resolved' || s === 'closed' || s === 'done';
          }).length;
          const percentage = total > 0 ? Math.round((completed / total) * 100) : 0;
          this.projectStats.set(project.id, { total, completed, percentage });
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error(`Error loading stats for project ${project.id}`, err);
        }
      });
    }
  }

  getProjectProgressPercentage(projectId: string): number {
    return this.projectStats.get(projectId)?.percentage ?? 0;
  }

  getProjectProgressLabel(projectId: string): string {
    const stats = this.projectStats.get(projectId);
    if (!stats) return '0 of 0 issues';
    return `${stats.completed} of ${stats.total} issues`;
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
