import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from './service/login.service';
import { GrantType } from '../shared/enums/grant-type.enum';
import { ToastService } from '../shared/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  rememberMe: boolean = false;

  constructor(
    private authService: AuthService,
    private toastService: ToastService
  ) { }

  onSubmit(): void {
    if (!this.username || !this.password) {
      this.toastService.showWarning(
        'Campos Obrigatórios',
        'Por favor, preencha usuário e senha.'
      );
      return;
    }
    
    this.toastService.showInfo(
      'Login',
      'Funcionalidade de login com usuário/senha em desenvolvimento.'
    );
  }

  loginWithGoogle(): void {
    const grantType = GrantType.GOOGLE;
    this.authService.login(this.username, this.password, grantType);
  }

  loginWithInstagram(): void {
    const grantType = GrantType.INSTAGRAM;
    this.authService.login(this.username, this.password, grantType);
  }
}
