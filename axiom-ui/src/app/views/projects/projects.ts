import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { finalize } from 'rxjs';
import { CreateProjectRequest, ProjectDto, ProjectsApi } from '../../services/projects-api';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CardModule, InputTextModule],
  templateUrl: './projects.html',
})
export class ProjectsComponent {
  projects: ProjectDto[] = [];
  loading = false;
  creating = false;
  errorMessage = '';
  createErrorMessage = '';

  form = {
    name: '',
    code: '',
    description: '',
    ownerId: '00000000-0000-0000-0000-000000000001',
  };

  constructor(private readonly projectsApi: ProjectsApi) {
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

    this.projectsApi
      .getProjects()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (projects) => {
          this.projects = projects;
        },
        error: () => {
          this.errorMessage = 'Could not load projects. Check if the API is running.';
        },
      });
  }

  createProject(): void {
    this.creating = true;
    this.createErrorMessage = '';

    this.projectsApi
      .createProject(this.toRequest())
      .pipe(finalize(() => (this.creating = false)))
      .subscribe({
        next: () => {
          this.resetForm();
          this.loadProjects();
        },
        error: () => {
          this.createErrorMessage = 'Could not create project. Check required fields and owner id.';
        },
      });
  }

  private toRequest(): CreateProjectRequest {
    const description = this.form.description.trim();

    return {
      name: this.form.name.trim(),
      code: this.form.code.trim().toUpperCase(),
      description: description || null,
      ownerId: this.form.ownerId.trim(),
    };
  }

  private resetForm(): void {
    this.form = {
      name: '',
      code: '',
      description: '',
      ownerId: this.form.ownerId,
    };
  }
}
