import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toasts$ = new BehaviorSubject<Toast[]>([]);

  getToasts() {
    return this.toasts$.asObservable();
  }

  showToast(toast: Omit<Toast, 'id'>) {
    const newToast: Toast = {
      ...toast,
      id: this.generateId(),
      duration: toast.duration || 5000
    };

    const currentToasts = this.toasts$.value;
    this.toasts$.next([...currentToasts, newToast]);

    // Auto remove after duration
    if (newToast.duration && newToast.duration > 0) {
      setTimeout(() => this.removeToast(newToast.id), newToast.duration);
    }
  }

  showError(title: string, message: string) {
    console.log('Showing error toast:', title, message);
    this.showToast({
      type: 'error',
      title,
      message
    });
  }

  showSuccess(title: string, message: string) {
    console.log('Showing success toast:', title, message);
    this.showToast({
      type: 'success',
      title,
      message
    });
  }

  showWarning(title: string, message: string) {
    console.log('Showing warning toast:', title, message);
    this.showToast({
      type: 'warning',
      title,
      message
    });
  }

  showInfo(title: string, message: string) {
    console.log('Showing info toast:', title, message);
    this.showToast({
      type: 'info',
      title,
      message
    });
  }

  removeToast(id: string) {
    const currentToasts = this.toasts$.value;
    this.toasts$.next(currentToasts.filter(toast => toast.id !== id));
  }

  clearAll() {
    this.toasts$.next([]);
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }
}