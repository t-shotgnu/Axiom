import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { finalize } from 'rxjs';

import { Project, ProjectService } from '../../core/services/project.service';
import { ButtonComponent } from '../../shared/components/ui/button';
import { CardComponent } from '../../shared/components/ui/card';
import { DialogComponent } from '../../shared/components/ui/dialog';
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
    DialogComponent,
    InputComponent,
    ProjectMembersPanelComponent,
  ],
  templateUrl: './project-settings.html',
})
export class ProjectSettingsComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly destroyRef = inject(DestroyRef);

  project = signal<Project | null>(null);
  loadingProject = signal(false);
  savingProject = signal(false);
  projectSaveError = signal('');
  canManageProject = signal(false);

  // Delete project state
  showDeleteDialog = signal(false);
  deletingProject = signal(false);
  projectDeleteError = signal('');

  get showDeleteDialogVisible() { return this.showDeleteDialog(); }
  set showDeleteDialogVisible(v: boolean) { this.showDeleteDialog.set(v); }

  form = {
    name: '',
    code: '',
  };

  constructor() {
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        const projectId = params.get('id');
        if (projectId) {
          this.loadProject(projectId);
        }
      });

    // React to project switching from the sidebar dropdown
    this.projectService.currentProject$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((currentProject) => {
        const routeId = this.route.snapshot.paramMap.get('id');
        if (currentProject && currentProject.id !== routeId) {
          void this.router.navigate(['/projects', currentProject.id, 'settings']);
        }
      });
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

  openDeleteProjectDialog(): void {
    this.projectDeleteError.set('');
    this.showDeleteDialog.set(true);
  }

  cancelDeleteProjectDialog(): void {
    this.showDeleteDialog.set(false);
    this.projectDeleteError.set('');
  }

  confirmDeleteProject(): void {
    const project = this.project();
    if (!project || this.deletingProject()) {
      return;
    }

    this.deletingProject.set(true);
    this.projectDeleteError.set('');

    this.projectService
      .deleteProject(project.id)
      .pipe(
        finalize(() => this.deletingProject.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.showDeleteDialog.set(false);
          void this.router.navigate(['/projects']);
        },
        error: (err) => {
          console.error(err);
          this.projectDeleteError.set('Could not delete the project. Please try again.');
        },
      });
  }
}
