import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { finalize } from 'rxjs';

import { ProjectService, Project } from '../../core/services/project.service';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../../shared/components/ui/button';
import { CardComponent } from '../../shared/components/ui/card';
import { DialogComponent } from '../../shared/components/ui/dialog';
import { ProjectMembersPanelComponent } from './components/project-members-panel';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [
    RouterModule,
    CommonModule,
    ButtonComponent,
    CardComponent,
    DialogComponent,
    ProjectMembersPanelComponent,
  ],
  templateUrl: './project-detail.html',
})
export class ProjectDetailComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private projectService = inject(ProjectService);
  private destroyRef = inject(DestroyRef);

  project = signal<Project | null>(null);
  loadingProject = signal(false);
  showDeleteProjectDialog = signal(false);
  deletingProject = signal(false);
  projectToDelete = signal<Project | null>(null);
  projectDeleteErrorMessage = signal('');
  canManageProject = signal(false);

  constructor() {
    const projectId = this.route.snapshot.paramMap.get('id')!;
    this.loadProject(projectId);
  }

  get showDeleteProjectDialogVisible() { return this.showDeleteProjectDialog(); }
  set showDeleteProjectDialogVisible(v: boolean) { this.showDeleteProjectDialog.set(v); }

  loadProject(id: string) {
    this.loadingProject.set(true);
    this.projectService.getProjectById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (p) => { this.project.set(p); this.loadingProject.set(false); },
        error: (err) => { console.error(err); this.loadingProject.set(false); },
      });
  }

  openDeleteProjectDialog(): void {
    if (!this.canManageProject()) {
      return;
    }

    this.projectToDelete.set(this.project());
    this.projectDeleteErrorMessage.set('');
    this.showDeleteProjectDialog.set(true);
  }

  cancelDeleteProjectDialog(): void {
    this.showDeleteProjectDialog.set(false);
    this.projectToDelete.set(null);
    this.projectDeleteErrorMessage.set('');
  }

  confirmDeleteProject(): void {
    const project = this.projectToDelete();
    if (!project || this.deletingProject()) {
      return;
    }

    this.deletingProject.set(true);
    this.projectDeleteErrorMessage.set('');

    this.projectService
      .deleteProject(project.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.deletingProject.set(false)),
      )
      .subscribe({
        next: () => this.router.navigate(['/projects']),
        error: (err) => {
          console.error(err);
          this.projectDeleteErrorMessage.set('Could not delete the project. Please try again.');
        },
      });
  }

}
