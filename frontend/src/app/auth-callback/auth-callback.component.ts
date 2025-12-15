
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AuthService } from '../login/service/auth.service';
import { CommonModule } from '@angular/common';
import { ToastService } from '../shared/services/toast.service';

@Component({
  selector: 'app-auth-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <svg viewBox="0 0 24 24" fill="currentColor" class="logo-icon">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
          <h1>Autenticando...</h1>
          <p *ngIf="loading">Aguarde enquanto confirmamos sua autenticação.</p>
          <p *ngIf="error" style="color:#ef4444;">❌ {{ error }}</p>
        </div>
        <div class="social-login" *ngIf="error">
          <button class="social-btn logout-btn" (click)="goToLogin()">
            <span>Tentar novamente</span>
          </button>
        </div>
        <div *ngIf="loading" class="spinner" style="margin:2rem auto;"></div>
      </div>
    </div>
  `,
  styleUrls: ['../login/login.component.scss']
})
export class AuthCallbackComponent implements OnInit {
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['code']) {
        this.authService.handleCallback(params['code']);
      } else if (params['error']) {
        this.handleParamsError(params);
      } else {
        this.handleGeneralError();
      }
    });
  }

  public goToLogin(): void {
    this.router.navigate(['/login']);
  }

  private handleParamsError(params: Params): void {
    const errorMessage = params['error_description'] || 'Erro na autenticação';
    this.error = errorMessage;
    this.loading = false;
    this.toastService.showError(
      'Erro na Autenticação',
      errorMessage
    );
  }

  private handleGeneralError(): void {
    const errorMessage = 'Resposta inválida do servidor de autenticação';
    this.error = errorMessage;
    this.loading = false;
    this.toastService.showError(
      'Erro na Autenticação',
      errorMessage
    );
  }
  
}