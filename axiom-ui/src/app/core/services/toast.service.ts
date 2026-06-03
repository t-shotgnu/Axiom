import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  toasts = signal<Toast[]>([]);

  show(message: string, type: 'success' | 'error' | 'info' = 'success', duration = 3000): void {
    const id = Math.random().toString(36).substring(2, 9);
    const newToast: Toast = { id, message, type };
    this.toasts.update(current => [...current, newToast]);

    setTimeout(() => {
      this.toasts.update(current => current.filter(t => t.id !== id));
    }, duration);
  }

  success(message: string, duration = 3000): void {
    this.show(message, 'success', duration);
  }

  error(message: string, duration = 3000): void {
    this.show(message, 'error', duration);
  }

  info(message: string, duration = 3000): void {
    this.show(message, 'info', duration);
  }
}
