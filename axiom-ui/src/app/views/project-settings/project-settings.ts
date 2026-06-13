import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { finalize } from 'rxjs';

import { Project, ProjectService } from '../../core/services/project.service';
import { ButtonComponent } from '../../shared/components/ui/button';
import { CardComponent } from '../../shared/components/ui/card';
import { InputComponent } from '../../shared/components/ui/input';
import { ProjectMembersPanelComponent } from '../project-detail/components/project-members-panel';

@Component({
  selector: 'app-project-settings',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    CardComponent,
    ButtonComponent,
    InputComponent,
    ProjectMembersPanelComponent,
  ],
  templateUrl: './project-settings.html',
})
export class ProjectSettingsComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly destroyRef = inject(DestroyRef);

  project = signal<Project | null>(null);
  loadingProject = signal(false);
  savingProject = signal(false);
  projectSaveError = signal('');
  canManageProject = signal(false);

  form = {
    name: '',
    code: '',
  };

  constructor() {
    const projectId = this.route.snapshot.paramMap.get('id')!;
    this.loadProject(projectId);
  }

  loadProject(id: string): void {
    this.loadingProject.set(true);
    this.projectService
      .getProjectById(id)
      .pipe(
        finalize(() => this.loadingProject.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (project) => {
          this.project.set(project);
          this.form.name = project.name;
          this.form.code = project.code;
        },
        error: (err) => console.error(err),
      });
  }

  saveProjectDetails(): void {
    const project = this.project();
    if (!project || !this.canManageProject() || !this.form.name.trim() || !this.form.code.trim()) {
      return;
    }

    this.savingProject.set(true);
    this.projectSaveError.set('');

    this.projectService
      .updateProject(project.id, {
        name: this.form.name.trim(),
        code: this.form.code.trim().toUpperCase(),
      })
      .pipe(
        finalize(() => this.savingProject.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.loadProject(project.id);
          this.projectService.setCurrentProject({
            ...project,
            name: this.form.name.trim(),
            code: this.form.code.trim().toUpperCase(),
          });
        },
        error: (err) => {
          console.error(err);
          this.projectSaveError.set('Could not update project name or key. Check your permissions and try again.');
        },
      });
  }
}
