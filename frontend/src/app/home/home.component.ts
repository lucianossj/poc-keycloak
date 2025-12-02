import { Component } from '@angular/core';
import { AuthService } from '../login/service/login.service';
@Component({
  selector: 'app-home',
  template: `
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <svg viewBox="0 0 24 24" fill="currentColor" class="logo-icon">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
          <h1>Bem-vindo!</h1>
          <p>Você está logado no POC Keycloak.</p>
        </div>
        <div class="social-login">
          <button class="social-btn logout-btn" (click)="logout()">
            <span>Sair</span>
          </button>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['../login/login.component.scss']
})
export class HomeComponent {
  constructor(private auth: AuthService) {}
  logout() {
    this.auth.logout();
  }
}
