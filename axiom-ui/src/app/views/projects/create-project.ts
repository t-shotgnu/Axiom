import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { CreateProjectCommand, ProjectService } from '../../core/services/project.service';
import { ButtonComponent } from '../../shared/components/ui/button';

@Component({
  selector: 'app-create-project',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonComponent],
  templateUrl: './create-project.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateProjectComponent {
  @Output() projectCreated = new EventEmitter<void>();

  displayCreateDialog = false;
  creating = false;
  createErrorMessage = '';

  form = {
    name: '',
    code: '',
    description: '',
  };

  constructor(
    private readonly projectService: ProjectService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  show(): void {
    this.resetForm();
    this.createErrorMessage = '';
    this.displayCreateDialog = true;
    this.cdr.markForCheck();
  }

  createProject(): void {
    if (!this.form.name.trim() || !this.form.code.trim()) {
      return;
    }

    this.creating = true;
    this.createErrorMessage = '';

    this.projectService
      .createProject(this.toCommand())
      .pipe(
        finalize(() => {
          this.creating = false;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: () => {
          this.resetForm();
          this.displayCreateDialog = false;
          this.projectCreated.emit();
        },
        error: (err: unknown) => {
          this.createErrorMessage = this.mapProjectCreateError(err);
          this.cdr.markForCheck();
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

  private mapProjectCreateError(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401 || err.status === 403) {
        return 'Your session expired or you are not allowed to create projects. Sign in again, then retry.';
      }
      if (err.status === 409) {
        return 'A project with this code may already exist. Try another code.';
      }
      if (err.status === 400) {
        const apiMessage = this.extractApiMessage(err);
        if (apiMessage) {
          return apiMessage;
        }
        return 'Could not create project. Check that name and code meet validation rules.';
      }
      if (err.status === 0) {
        return 'Could not reach the API. Check your network or that the backend is running.';
      }
    }
    return 'Could not create project. Check name and code.';
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
