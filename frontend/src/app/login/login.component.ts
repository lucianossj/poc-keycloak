import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './service/login.service';
import { ToastService } from '../shared/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email: string = '';
  password: string = '';
  loading: boolean = false;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private router: Router,
    private toastService: ToastService
  ) { }

  loginWithGoogle(): void {
    this.authService.getGoogleAuthUrl();
  }

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.toastService.showError('Erro', 'Preencha e-mail e senha');
      return;
    }

    this.loading = true;

    const loginData = {
      email: this.email,
      password: this.password
    };

    this.http.post('http://localhost:8081/auth/login', loginData).subscribe({
      next: (response: any) => {
        console.log('Login bem-sucedido:', response);
        localStorage.setItem('access_token', response.access_token);
        localStorage.setItem('refresh_token', response.refresh_token);
        
        if (response.id_token) {
          localStorage.setItem('id_token', response.id_token);
        }
        
        this.toastService.showSuccess('Sucesso', 'Login realizado com sucesso!');
        
        if (response.is_first_login) {
          this.router.navigate(['/complete-profile']);
        } else {
          this.router.navigate(['/home']);
        }
      },
      error: (error) => {
        console.error('Erro no login:', error);
        this.toastService.showError('Erro', 'E-mail ou senha inválidos');
        this.loading = false;
      }
    });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}
