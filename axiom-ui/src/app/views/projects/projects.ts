import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ProjectService, Project, CreateProjectCommand } from '../../core/services/project.service';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [RouterModule, FormsModule, CardModule, ButtonModule, InputTextModule, ProgressSpinnerModule],
  templateUrl: './projects.html',
})
export class ProjectsComponent {
  private projectService = inject(ProjectService);
  private destroyRef = inject(DestroyRef);

  projects = signal<Project[]>([]);
  loading = signal(false);

  newProject: Partial<CreateProjectCommand> = {
    name: '',
    code: '',
    description: '',
  };

  constructor() {
    this.loadProjects();
  }

  loadProjects() {
    this.loading.set(true);
    this.projectService
      .getAllProjects()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.projects.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          console.error(err);
          this.loading.set(false);
        },
      });
  }

  createProject() {
    if (!this.newProject.name || !this.newProject.code) return;

    this.projectService
      .createProject(this.newProject as CreateProjectCommand)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.newProject = { name: '', code: '', description: '' };
          this.loadProjects();
        },
        error: (err) => console.error(err),
      });
  }
}
