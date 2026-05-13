import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { finalize } from 'rxjs';
import { CreateProjectRequest, ProjectDto, ProjectsApi } from '../../services/projects-api';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, InputTextModule],
  template: `
    <div class="grid gap-6">
      <header class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="text-sm uppercase tracking-wide text-slate-500">Workspace</p>
          <h1 class="text-2xl font-semibold">Projects</h1>
        </div>
        <button
          pButton
          type="button"
          label="Refresh"
          class="p-button-outlined"
          [disabled]="loading"
          (click)="loadProjects()"
        ></button>
      </header>

      <section class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_24rem]">
        <div class="grid gap-3">
          @if (loading) {
            <div class="rounded bg-white p-4 text-slate-500 shadow-sm">Loading projects...</div>
          } @else if (errorMessage) {
            <div class="rounded border border-red-200 bg-red-50 p-4 text-red-700">
              {{ errorMessage }}
            </div>
          } @else if (projects.length === 0) {
            <div class="rounded bg-white p-6 text-center text-slate-500 shadow-sm">
              No projects yet. Create the first one from the form.
            </div>
          } @else {
            @for (project of projects; track project.id) {
              <article class="rounded bg-white p-4 shadow-sm">
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="rounded bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-700">
                        {{ project.code }}
                      </span>
                      <h2 class="font-semibold">{{ project.name }}</h2>
                    </div>
                    <p class="mt-2 text-sm text-slate-600">
                      {{ project.description || 'No description provided.' }}
                    </p>
                  </div>
                  <time class="text-xs text-slate-400">{{ project.createdOn | date: 'short' }}</time>
                </div>
                <p class="mt-3 text-xs text-slate-400">Owner: {{ project.ownerId }}</p>
              </article>
            }
          }
        </div>

        <form class="rounded bg-white p-4 shadow-sm" (ngSubmit)="createProject()">
          <h2 class="text-lg font-semibold">Create project</h2>
          <p class="mt-1 text-sm text-slate-500">
            Backend still expects an owner id until auth context is merged.
          </p>

          <div class="mt-4 grid gap-3">
            <label class="grid gap-1 text-sm">
              Name
              <input
                pInputText
                name="name"
                required
                [(ngModel)]="form.name"
                placeholder="Website Redesign"
              />
            </label>

            <label class="grid gap-1 text-sm">
              Code
              <input
                pInputText
                name="code"
                required
                [(ngModel)]="form.code"
                placeholder="WEB"
              />
            </label>

            <label class="grid gap-1 text-sm">
              Description
              <textarea
                name="description"
                rows="4"
                class="rounded border border-slate-300 p-2 text-sm outline-none focus:border-primary"
                [(ngModel)]="form.description"
                placeholder="Short project description"
              ></textarea>
            </label>

            <label class="grid gap-1 text-sm">
              Owner ID
              <input pInputText name="ownerId" required [(ngModel)]="form.ownerId" />
            </label>
          </div>

          @if (createErrorMessage) {
            <div class="mt-4 rounded border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              {{ createErrorMessage }}
            </div>
          }

          <button
            pButton
            type="submit"
            label="Create project"
            class="mt-4 w-full"
            [disabled]="creating || !form.name.trim() || !form.code.trim() || !form.ownerId.trim()"
          ></button>
        </form>
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
