import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, finalize } from 'rxjs';

import { ProjectService, Project } from '../../core/services/project.service';
import { ProjectMember, ProjectMemberService } from '../../core/services/project-member.service';

@Component({
  selector: 'app-team',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './team.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TeamComponent implements OnInit, OnDestroy {
  private readonly projectService = inject(ProjectService);
  private readonly memberService = inject(ProjectMemberService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);

  private projectSub?: Subscription;

  currentProject: Project | null = null;
  members: ProjectMember[] = [];
  loading = true;

  ngOnInit(): void {
    this.projectSub = this.projectService.currentProject$.subscribe({
      next: (project) => {
        this.currentProject = project;
        this.loading = true;
        this.cdr.markForCheck();

        if (project) {
          this.loadMembers(project.id);
        } else {
          this.members = [];
          this.loading = false;
          this.cdr.markForCheck();
        }
      },
    });
  }

  ngOnDestroy(): void {
    this.projectSub?.unsubscribe();
  }

  private loadMembers(projectId: string): void {
    this.memberService
      .getProjectMembers(projectId)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (members) => {
          this.members = members;
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Error loading team members', err);
          this.members = [];
          this.cdr.markForCheck();
        },
      });
  }

  getMemberDisplayName(member: ProjectMember): string {
    const fullName = `${member.firstName || ''} ${member.lastName || ''}`.trim();
    return fullName || member.userName || member.emailAddress;
  }

  getMemberInitial(member: ProjectMember): string {
    return this.getMemberDisplayName(member).substring(0, 1).toUpperCase();
  }

  getRoleBadgeClass(member: ProjectMember): string {
    if (this.isLead(member)) {
      return 'bg-primary/10 text-primary';
    }
    if (member.role === 'ADMIN') {
      return 'bg-violet-500/10 text-violet-700';
    }
    return 'bg-surface-variant text-on-surface-variant';
  }

  getRoleLabel(member: ProjectMember): string {
    if (this.isLead(member)) {
      return 'Lead';
    }
    return member.role === 'ADMIN' ? 'Admin' : 'Member';
  }

  isLead(member: ProjectMember): boolean {
    return !!this.currentProject && member.userId === this.currentProject.ownerId;
  }
}
