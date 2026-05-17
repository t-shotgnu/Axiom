import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    inject,
    Input,
    Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ButtonComponent } from '../ui/button';

interface ChangePasswordForm {
    oldPassword: string;
    newPassword: string;
    newPasswordConfirmation: string;
}

@Component({
    selector: 'app-change-password',
    standalone: true,
    imports: [CommonModule, FormsModule, ButtonComponent],
    templateUrl: './change-password.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChangePasswordComponent {
    private readonly auth = inject(AuthService);
    private readonly cdr = inject(ChangeDetectorRef);

    @Input() display = false;
    @Output() displayChange = new EventEmitter<boolean>();
    @Output() closed = new EventEmitter<void>();

    protected form: ChangePasswordForm = {
        oldPassword: '',
        newPassword: '',
        newPasswordConfirmation: '',
    };

    protected loading = false;
    protected errorMessage = '';
    protected successMessage = '';
    protected fieldErrors: { [key: string]: string } = {};

    protected showOld = false;
    protected showNew = false;
    protected showConfirm = false;

    get passwordMismatch(): boolean {
        return (
            !!this.form.newPasswordConfirmation &&
            this.form.newPassword !== this.form.newPasswordConfirmation
        );
    }

    get passwordStrengthPercentage(): number {
        const pwd = this.form.newPassword || '';
        if (!pwd) return 0;

        let score = 0;
        if (pwd.length >= 8) score += 25;
        if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) score += 25;
        if (/[0-9]/.test(pwd)) score += 25;
        if (pwd.length >= 12) score += 25;
        return Math.min(100, score);
    }

    get passwordStrengthText(): string {
        const p = this.passwordStrengthPercentage;
        if (p === 0) return '';
        if (p <= 25) return 'Weak';
        if (p <= 50) return 'Fair';
        if (p <= 75) return 'Good';
        return 'Strong';
    }

    get passwordMeetsRegistrationRules(): boolean {
        const pwd = this.form.newPassword || '';
        return (
            pwd.length >= 8 && /[A-Z]/.test(pwd) && /[a-z]/.test(pwd) && /[0-9]/.test(pwd)
        );
    }

    get samePassword(): boolean {
        return (
            !!this.form.oldPassword &&
            !!this.form.newPassword &&
            this.form.oldPassword === this.form.newPassword
        );
    }

    close(): void {
        this.reset();
        this.display = false;
        this.displayChange.emit(false);
        this.closed.emit();
    }

    submit(): void {
        if (this.passwordMismatch || this.samePassword || this.loading) return;

        this.loading = true;
        this.errorMessage = '';
        this.successMessage = '';
        this.fieldErrors = {};

        this.auth
            .changePassword({
                oldPassword: this.form.oldPassword,
                newPassword: this.form.newPassword,
                newPasswordConfirmation: this.form.newPasswordConfirmation,
            })
            .subscribe({
                next: () => {
                    this.loading = false;
                    this.successMessage = 'Password changed successfully.';
                    this.form = { oldPassword: '', newPassword: '', newPasswordConfirmation: '' };
                    this.cdr.markForCheck();
                    setTimeout(() => this.close(), 1500);
                },
                error: (err) => {
                    this.loading = false;
                    this.errorMessage =
                        err?.error?.detail ??
                        err?.message ??
                        'Unable to change password. Please try again.';
                    this.cdr.markForCheck();
                },
            });
    }

    private reset(): void {
        this.form = { oldPassword: '', newPassword: '', newPasswordConfirmation: '' };
        this.errorMessage = '';
        this.fieldErrors = {};
        this.successMessage = '';
        this.loading = false;
        this.showOld = false;
        this.showNew = false;
        this.showConfirm = false;
    }
}