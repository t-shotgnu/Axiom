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
  template: `
    <div class="grid gap-6 text-zinc-900 dark:text-zinc-100">
      <header class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="text-sm uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Workspace</p>
          <h1 class="text-2xl font-semibold">Projects</h1>
        </div>
        <p-button
          label="Refresh"
          icon="pi pi-refresh"
          severity="secondary"
          [outlined]="true"
          [disabled]="loading"
          (onClick)="loadProjects()"
        ></p-button>
      </header>

      <section class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_24rem]">
        <div class="grid gap-4">
          @if (loading) {
            <p-card>
              <p class="text-zinc-500 dark:text-zinc-400">Loading projects...</p>
            </p-card>
          } @else if (errorMessage) {
            <div class="rounded border border-red-200 bg-red-50 p-4 text-red-700 dark:border-red-900 dark:bg-red-950/50 dark:text-red-300">
              {{ errorMessage }}
            </div>
          } @else if (projects.length === 0) {
            <p-card>
              <p class="text-center text-zinc-500 dark:text-zinc-400">
                No projects yet. Create the first one from the form.
              </p>
            </p-card>
          } @else {
            @for (project of projects; track project.id) {
              <p-card styleClass="shadow-sm border border-zinc-200 dark:border-zinc-700">
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="rounded-full border border-zinc-200 bg-zinc-100 px-3 py-1 text-xs font-semibold text-zinc-700 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-200">
                        {{ project.code }}
                      </span>
                      <h2 class="text-lg font-medium">{{ project.name }}</h2>
                    </div>
                    <p class="mt-2 text-sm text-zinc-500 dark:text-zinc-400">
                      {{ project.description || 'No description provided.' }}
                    </p>
                  </div>
                  <time class="text-xs text-zinc-400 dark:text-zinc-500">{{ project.createdOn | date: 'short' }}</time>
                </div>
                <p class="mt-3 font-mono text-xs text-zinc-400 dark:text-zinc-500">Owner: {{ project.ownerId }}</p>
              </p-card>
            }
          }
        </div>

        <p-card header="Create Project" styleClass="shadow-sm border border-zinc-200 dark:border-zinc-700">
          <form class="grid gap-4" (ngSubmit)="createProject()">
            <p class="text-sm text-zinc-500 dark:text-zinc-400">
              Owner id is still required until project ownership is moved to the session.
            </p>

            <div class="flex flex-col gap-2">
              <label class="text-sm font-semibold text-zinc-700 dark:text-zinc-300">Name</label>
              <input pInputText name="name" required [(ngModel)]="form.name" placeholder="Website Redesign" />
            </div>

            <div class="flex flex-col gap-2">
              <label class="text-sm font-semibold text-zinc-700 dark:text-zinc-300">Code</label>
              <input pInputText name="code" required [(ngModel)]="form.code" placeholder="WEB" />
            </div>

            <div class="flex flex-col gap-2">
              <label class="text-sm font-semibold text-zinc-700 dark:text-zinc-300">Description</label>
              <textarea
                name="description"
                rows="4"
                class="rounded border border-zinc-300 p-2 text-sm outline-none focus:border-primary dark:border-zinc-700 dark:bg-zinc-900"
                [(ngModel)]="form.description"
                placeholder="Short project description"
              ></textarea>
            </div>

            <div class="flex flex-col gap-2 rounded-lg border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-700/50 dark:bg-zinc-800/50">
              <label class="text-xs font-semibold text-zinc-600 dark:text-zinc-400">Owner ID</label>
              <input pInputText name="ownerId" required class="font-mono text-sm" [(ngModel)]="form.ownerId" />
            </div>

            @if (createErrorMessage) {
              <div class="rounded border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/50 dark:text-red-300">
                {{ createErrorMessage }}
              </div>
            }

            <p-button
              type="submit"
              label="Create Project"
              icon="pi pi-plus"
              styleClass="w-full"
              [disabled]="creating || !form.name.trim() || !form.code.trim() || !form.ownerId.trim()"
            ></p-button>
          </form>
        </p-card>
      </section>
    </div>
  `,
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
