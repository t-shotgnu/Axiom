import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, Toast } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed bottom-6 right-6 z-[100] flex flex-col gap-sm pointer-events-none max-w-[360px] w-full">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          [class]="getToastClasses(toast)"
          class="flex items-center gap-md px-md py-sm rounded-xl shadow-lg border pointer-events-auto animate-toast-slide"
          role="alert"
        >
          <!-- Icon -->
          <span class="material-symbols-outlined text-[20px] shrink-0 font-bold">
            {{ getIcon(toast.type) }}
          </span>
          
          <!-- Message -->
          <span class="text-body-sm font-semibold leading-tight">{{ toast.message }}</span>
          
          <!-- Close Button -->
          <button
            (click)="remove(toast.id)"
            class="ml-auto flex h-6 w-6 items-center justify-center rounded-full hover:bg-black/5 dark:hover:bg-white/5 transition-colors shrink-0 text-inherit/60 hover:text-inherit"
          >
            <span class="material-symbols-outlined text-[16px]">close</span>
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    @keyframes toastSlide {
      from {
        opacity: 0;
        transform: translateY(16px) scale(0.95);
      }
      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }
    .animate-toast-slide {
      animation: toastSlide 0.2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }
  `]
})
export class ToastContainerComponent {
  protected readonly toastService = inject(ToastService);

  remove(id: string): void {
    this.toastService.toasts.update(current => current.filter(t => t.id !== id));
  }

  getIcon(type: 'success' | 'error' | 'info'): string {
    switch (type) {
      case 'success':
        return 'check_circle';
      case 'error':
        return 'error';
      case 'info':
      default:
        return 'info';
    }
  }

  getToastClasses(toast: Toast): string {
    const base = 'transition-all duration-200';
    switch (toast.type) {
      case 'success':
        return `${base} bg-green-500/10 border-green-500/20 text-green-600 dark:text-green-400`;
      case 'error':
        return `${base} bg-red-500/10 border-red-500/20 text-red-600 dark:text-red-400`;
      case 'info':
      default:
        return `${base} bg-primary/10 border-primary/20 text-primary dark:text-primary-light`;
    }
  }
}
