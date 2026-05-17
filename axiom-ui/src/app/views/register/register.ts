import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, RegisterUserCommand } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="flex min-h-screen items-center justify-center bg-background px-4 py-12">
      <div class="w-full max-w-[520px] flex flex-col items-center">
        <div class="flex flex-col items-center mb-lg text-center">
          <div class="flex items-center gap-xs mb-sm">
            <span class="material-symbols-outlined text-[32px] text-primary font-bold">grid_view</span>
            <span class="text-primary font-extrabold text-[28px] font-headline-md tracking-tight">Axiom</span>
          </div>
          <p class="text-on-surface-variant font-medium text-body-md">
            Utwórz konto i uzupełnij dane profilu.
          </p>
        </div>

        <div class="w-full bg-surface-container-lowest border border-outline-variant rounded-lg p-lg shadow-xl">
          <div *ngIf="error" class="rounded border border-red-200 bg-red-50 p-sm text-sm text-red-800 font-semibold mb-lg" role="alert">
            {{ error }}
          </div>

          <form class="space-y-lg" (ngSubmit)="register()">
            <div class="grid gap-sm sm:grid-cols-2">
              <div class="flex flex-col gap-xs">
                <label class="text-[11px] font-bold text-on-surface-variant tracking-wider uppercase">Nazwa użytkownika</label>
                <input type="text" name="userName" [(ngModel)]="command.userName" placeholder="np. jan.kowalski" class="w-full px-md py-[10px] border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface-container-low text-on-surface text-sm placeholder:text-on-surface-variant/60 transition-colors" required />
                @if (command.userName && !isUserNameValid(command.userName)) {
                  <p class="text-xs text-error font-medium">Nazwa użytkownika powinna mieć 3-30 znaków.</p>
                }
              </div>
              <div class="flex flex-col gap-xs">
                <label class="text-[11px] font-bold text-on-surface-variant tracking-wider uppercase">Data urodzenia</label>
                <input type="date" name="dateOfBirth" [(ngModel)]="command.dateOfBirth" [max]="todayDate" class="w-full px-md py-[10px] border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface-container-low text-on-surface text-sm transition-colors" required />
                @if (command.dateOfBirth && !isDateOfBirthValid(command.dateOfBirth)) {
                  <p class="text-xs text-error font-medium">Data urodzenia nie może być z przyszłości.</p>
                }
              </div>
            </div>

            <div class="grid gap-sm sm:grid-cols-2">
              <div class="flex flex-col gap-xs">
                <label class="text-[11px] font-bold text-on-surface-variant tracking-wider uppercase">Imię</label>
                <input type="text" name="firstName" [(ngModel)]="command.firstName" placeholder="np. Jan" class="w-full px-md py-[10px] border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface-container-low text-on-surface text-sm placeholder:text-on-surface-variant/60 transition-colors" required />
                @if (command.firstName && !isNameValid(command.firstName)) {
                  <p class="text-xs text-error font-medium">Wpisz poprawne imię.</p>
                }
              </div>
              <div class="flex flex-col gap-xs">
                <label class="text-[11px] font-bold text-on-surface-variant tracking-wider uppercase">Nazwisko</label>
                <input type="text" name="lastName" [(ngModel)]="command.lastName" placeholder="np. Kowalski" class="w-full px-md py-[10px] border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface-container-low text-on-surface text-sm placeholder:text-on-surface-variant/60 transition-colors" required />
                @if (command.lastName && !isNameValid(command.lastName)) {
                  <p class="text-xs text-error font-medium">Wpisz poprawne nazwisko.</p>
                }
              </div>
            </div>

            <div class="flex flex-col gap-xs">
              <label class="text-[11px] font-bold text-on-surface-variant tracking-wider uppercase">Email</label>
              <input type="email" name="email" [(ngModel)]="command.emailAddress" placeholder="np. jan.kowalski@firma.pl" class="w-full px-md py-[10px] border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface-container-low text-on-surface text-sm placeholder:text-on-surface-variant/60 transition-colors" required />
              @if (command.emailAddress && !isEmailValid(command.emailAddress)) {
                <p class="text-xs text-error font-medium">Wpisz poprawny adres e-mail.</p>
              }
            </div>

            <div class="flex flex-col gap-xs">
              <label class="text-[11px] font-bold text-on-surface-variant tracking-wider uppercase">Hasło</label>
              <input type="password" name="password" [(ngModel)]="command.password" placeholder="Ustal hasło do konta" class="w-full px-md py-[10px] border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface-container-low text-on-surface text-sm placeholder:text-on-surface-variant/60 transition-colors" required />
              @if (command.password && !isPasswordValid(command.password)) {
                <p class="text-xs text-error font-medium">Hasło musi mieć min. 8 znaków, wielką literę i cyfrę.</p>
              }
            </div>

            <p class="text-xs text-on-surface-variant leading-normal">
              Dane profilu są wymagane do utworzenia konta i późniejszego uzupełnienia profilu użytkownika.
            </p>

            <div class="pt-sm">
              <button type="submit" class="w-full bg-primary text-on-primary font-label-md py-sm rounded hover:opacity-90 transition-opacity flex items-center justify-center gap-xs" [disabled]="loading || !canSubmit">
                <span *ngIf="loading" class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span>
                Utwórz konto
              </button>
            </div>
          </form>

          <div class="mt-xl text-center font-body-sm text-on-surface-variant">
            Masz już konto? <a routerLink="/login" class="text-primary hover:underline">Zaloguj się</a>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class RegisterComponent {
  private readonly cdr = inject(ChangeDetectorRef);

  command: RegisterUserCommand = {
    userName: '',
    emailAddress: '',
    password: '',
    firstName: '',
    lastName: '',
    dateOfBirth: '',
  };
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) { }

  get canSubmit(): boolean {
    return (
      this.isUserNameValid(this.command.userName) &&
      this.isEmailValid(this.command.emailAddress) &&
      this.isPasswordValid(this.command.password) &&
      this.isNameValid(this.command.firstName) &&
      this.isNameValid(this.command.lastName) &&
      this.isDateOfBirthValid(this.command.dateOfBirth)
    );
  }

  get todayDate(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  register() {
    this.error = '';
    this.loading = true;
    this.authService.register(this.command).subscribe({
      next: () => {
        this.loading = false;
        this.cdr.detectChanges();
        void this.router.navigate(['/projects']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Registration failed. Check inputs.';
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }

  isEmailValid(emailAddress: string): boolean {
    const value = emailAddress.trim();
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  }

  isUserNameValid(userName: string): boolean {
    const value = userName.trim();
    return value.length >= 3 && value.length <= 30;
  }

  isNameValid(name: string): boolean {
    const value = name.trim();
    return /^[A-Za-zÀ-ž' -]{2,40}$/.test(value);
  }

  isPasswordValid(password: string): boolean {
    return password.length >= 8 && /[A-Z]/.test(password) && /\d/.test(password);
  }

  isDateOfBirthValid(dateOfBirth: string): boolean {
    if (dateOfBirth.trim() === '') {
      return false;
    }

    const inputDate = new Date(`${dateOfBirth}T00:00:00`);
    if (Number.isNaN(inputDate.getTime())) {
      return false;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return inputDate <= today;
  }
}
