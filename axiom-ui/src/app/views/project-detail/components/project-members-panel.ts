import { CommonModule } from '@angular/common';
import { Component, DestroyRef, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import {
    AddProjectMemberCommand,
    ChangeProjectMemberRoleCommand,
    ProjectMember,
    ProjectMemberService,
} from '../../../core/services/project-member.service';
import { User, UserService } from '../../../core/services/user.service';
import { SelectComponent } from '../../../shared/components/ui/select';

@Component({
    selector: 'app-project-members-panel',
    standalone: true,
    imports: [CommonModule, FormsModule, SelectComponent],
    templateUrl: './project-members-panel.html',
})
export class ProjectMembersPanelComponent implements OnInit {
    private readonly memberService = inject(ProjectMemberService);
    private readonly userService = inject(UserService);
    private readonly destroyRef = inject(DestroyRef);

    roleOptions = [
        { label: 'Member', value: 'MEMBER' },
        { label: 'Admin', value: 'ADMIN' }
    ];

    get availableUserOptions() {
        const opts = [{ label: 'Select a user', value: '' }];
        for (const u of this.availableUsers) {
            opts.push({ label: this.getUserLabel(u), value: u.id });
        }
        return opts;
    }

    private _projectId = '';

    @Input()
    set projectId(value: string) {
        this._projectId = value;
        if (this.initialized && value) {
            this.loadData();
        }
    }

    get projectId(): string {
        return this._projectId;
    }

    @Input() leadName = '';
    @Input() leadUserId = '';
    @Output() canManageProjectChange = new EventEmitter<boolean>();

    members = signal<ProjectMember[]>([]);
    users = signal<User[]>([]);
    currentUserId = signal('');
    loadingMembers = signal(false);
    loadingUsers = signal(false);
    loadingCurrentUser = signal(false);
    memberActionError = signal('');
    selectedMemberUserId = signal('');
    selectedMemberRole = signal<'MEMBER' | 'ADMIN'>('MEMBER');

    private initialized = false;

    ngOnInit(): void {
        this.initialized = true;
        if (this.projectId) {
            this.loadData();
        }
        this.loadCurrentUser();
        this.updateCanManageProject();
    }

    get availableUsers(): User[] {
        const memberIds = new Set(this.members().map((member) => member.userId));
        return this.users().filter((user) => !memberIds.has(user.id));
    }

    loadData(): void {
        this.loadMembers();
        this.loadUsers();
    }

    loadCurrentUser(): void {
        this.loadingCurrentUser.set(true);
        this.userService
            .getCurrentUserProfile()
            .pipe(
                finalize(() => this.loadingCurrentUser.set(false)),
                takeUntilDestroyed(this.destroyRef),
            )
            .subscribe({
                next: (user) => {
                    this.currentUserId.set(user.id);
                    this.updateCanManageProject();
                },
                error: (err) => {
                    console.error(err);
                },
            });
    }

    loadMembers(): void {
        if (!this.projectId) {
            return;
        }

        this.loadingMembers.set(true);
        this.memberService
            .getProjectMembers(this.projectId)
            .pipe(
                finalize(() => this.loadingMembers.set(false)),
                takeUntilDestroyed(this.destroyRef),
            )
            .subscribe({
                next: (members) => {
                    this.members.set(members);
                    this.updateCanManageProject();
                },
                error: (err) => {
                    console.error(err);
                },
            });
    }

    loadUsers(): void {
        this.loadingUsers.set(true);
        this.userService
            .getAllUsers()
            .pipe(
                finalize(() => this.loadingUsers.set(false)),
                takeUntilDestroyed(this.destroyRef),
            )
            .subscribe({
                next: (users) => {
                    this.users.set(users);
                },
                error: (err) => {
                    console.error(err);
                },
            });
    }

    getMemberLabel(member: ProjectMember): string {
        const fullName = `${member.firstName || ''} ${member.lastName || ''}`.trim();
        return fullName || member.userName || member.emailAddress;
    }

    getUserLabel(user: User): string {
        const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();
        return fullName || user.userName || user.emailAddress;
    }

    isCurrentUser(member: ProjectMember): boolean {
        return member.userId === this.currentUserId();
    }

    isProjectLead(member: ProjectMember): boolean {
        return !!this.leadUserId && member.userId === this.leadUserId;
    }

    canRemoveMember(member: ProjectMember): boolean {
        return this.canManageProject() && !this.isCurrentUser(member) && !this.isProjectLead(member);
    }

    canManageProject(): boolean {
        const currentUserId = this.currentUserId();
        if (!currentUserId) {
            return false;
        }

        return this.members().some((member) => member.userId === currentUserId && member.role === 'ADMIN');
    }

    private updateCanManageProject(): void {
        this.canManageProjectChange.emit(this.canManageProject());
    }

    addMember(): void {
        const userId = this.selectedMemberUserId();
        if (!this.projectId || !userId) {
            return;
        }

        this.memberActionError.set('');
        const command: AddProjectMemberCommand = {
            userId,
            role: this.selectedMemberRole(),
        };

        this.memberService
            .addProjectMember(this.projectId, command)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.selectedMemberUserId.set('');
                    this.selectedMemberRole.set('MEMBER');
                    this.loadMembers();
                },
                error: (err) => {
                    console.error(err);
                    this.memberActionError.set('Could not add the user to the project.');
                },
            });
    }

    changeMemberRole(member: ProjectMember, role: 'MEMBER' | 'ADMIN'): void {
        if (!this.projectId) {
            return;
        }

        const command: ChangeProjectMemberRoleCommand = { role };
        this.memberActionError.set('');
        this.memberService
            .changeProjectMemberRole(this.projectId, member.userId, command)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => this.loadMembers(),
                error: (err) => {
                    console.error(err);
                    this.memberActionError.set('Could not change the member role.');
                },
            });
    }

    removeMember(member: ProjectMember): void {
        if (!this.projectId) {
            return;
        }

        if (this.isCurrentUser(member)) {
            this.memberActionError.set('You cannot remove yourself from the project.');
            return;
        }

        this.memberActionError.set('');
        this.memberService
            .removeProjectMember(this.projectId, member.userId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => this.loadMembers(),
                error: (err) => {
                    console.error(err);
                    this.memberActionError.set('Could not remove the member from the project.');
                },
            });
    }
}