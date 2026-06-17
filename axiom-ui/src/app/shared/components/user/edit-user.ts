import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
    inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { ButtonComponent } from '../ui/button';
import { InputComponent } from '../ui/input';
import { extractErrorMessage } from '../../../core/utils/error-utils';

interface EditProfileForm {
    firstName: string;
    lastName: string;
    dateOfBirth: string;
}

@Component({
    selector: 'app-edit-user',
    standalone: true,
    imports: [CommonModule, FormsModule, ButtonComponent, InputComponent],
    templateUrl: './edit-user.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditUserComponent implements OnChanges {
    private readonly userService = inject(UserService);
    private readonly cdr = inject(ChangeDetectorRef);

    @Input() display = false;
    @Output() displayChange = new EventEmitter<boolean>();
    @Output() saved = new EventEmitter<void>();

    protected loading = false;
    protected errorMessage = '';
    protected successMessage = '';
    protected fieldErrors: { [k: string]: string } = {};

    protected form: EditProfileForm = { firstName: '', lastName: '', dateOfBirth: '' };

    get todayDate(): string {
        const d = new Date();
        d.setHours(0, 0, 0, 0);
        return d.toISOString().split('T')[0];
    }

    isNameValid(name: string): boolean {
        const value = (name || '').trim();
        return /^[A-Za-zÀ-ž' -]{2,40}$/.test(value);
    }

    isDateOfBirthValid(dateOfBirth: string): boolean {
        if (!dateOfBirth || dateOfBirth.trim() === '') return false;
        const inputDate = new Date(`${dateOfBirth}T00:00:00`);
        if (Number.isNaN(inputDate.getTime())) return false;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return inputDate <= today;
    }

    get canSave(): boolean {
        return (
            !!this.form.firstName &&
            !!this.form.lastName &&
            this.isNameValid(this.form.firstName) &&
            this.isNameValid(this.form.lastName) &&
            this.isDateOfBirthValid(this.form.dateOfBirth)
        );
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['display'] && this.display) {
            this.loadProfile();
        }
    }

    close(): void {
        this.reset();
        this.display = false;
        this.displayChange.emit(false);
    }

    private loadProfile(): void {
        this.loading = true;
        this.errorMessage = '';

        this.userService.getCurrentUserProfile().subscribe({
            next: (u) => {
                this.loading = false;
                this.form = {
                    firstName: u.firstName ?? '',
                    lastName: u.lastName ?? '',
                    dateOfBirth: u.dateOfBirth ?? '',
                };
                this.cdr.markForCheck();
            },
            error: (err) => {
                this.loading = false;
                console.error('EditUserComponent: loadProfile error', err);
                this.errorMessage = extractErrorMessage(err, 'Failed to load profile.');
                this.cdr.markForCheck();
            },
        });
    }

    submit(): void {
        if (this.loading) return;
        this.loading = true;
        this.errorMessage = '';
        this.successMessage = '';
        this.fieldErrors = {};

        this.userService
            .updateCurrentUserProfile({
                firstName: this.form.firstName,
                lastName: this.form.lastName,
                dateOfBirth: this.form.dateOfBirth,
            })
            .subscribe({
                next: () => {
                    this.loading = false;
                    this.successMessage = 'Profile updated.';
                    this.cdr.markForCheck();
                    setTimeout(() => {
                        this.saved.emit();
                        this.close();
                    }, 900);
                },
                error: (err) => {
                    this.loading = false;
                    this.errorMessage = extractErrorMessage(err, 'Failed to update profile.');
                    this.cdr.markForCheck();
                },
            });
    }

    private reset(): void {
        this.form = { firstName: '', lastName: '', dateOfBirth: '' };
        this.errorMessage = '';
        this.successMessage = '';
        this.fieldErrors = {};
        this.loading = false;
    }
}
