import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../shared/services/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  name: string = '';
  email: string = '';
  document: string = '';
  birthDate: string = '';
  password: string = '';
  confirmPassword: string = '';
  loading: boolean = false;

  constructor(
    private http: HttpClient,
    private router: Router,
    private toastService: ToastService
  ) {}

  formatCPF(event: any) {
    let value = event.target.value.replace(/\D/g, '');
    if (value.length > 11) value = value.slice(0, 11);
    
    if (value.length > 9) {
      value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{1,2})/, '$1.$2.$3-$4');
    } else if (value.length > 6) {
      value = value.replace(/(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
    } else if (value.length > 3) {
      value = value.replace(/(\d{3})(\d{1,3})/, '$1.$2');
    }
    
    this.document = value;
  }

  onSubmit() {
    if (!this.name || !this.email || !this.document || !this.birthDate || !this.password) {
      this.toastService.showError('Erro', 'Preencha todos os campos');
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.toastService.showError('Erro', 'As senhas não coincidem');
      return;
    }

    if (this.password.length < 6) {
      this.toastService.showError('Erro', 'A senha deve ter no mínimo 6 caracteres');
      return;
    }

    this.loading = true;

    const registerData = {
      name: this.name,
      email: this.email,
      document: this.document.replace(/\D/g, ''),
      birthDate: this.birthDate,
      password: this.password
    };

    this.http.post('http://localhost:8081/auth/register', registerData).subscribe({
      next: (response: any) => {
        localStorage.setItem('access_token', response.access_token);
        localStorage.setItem('refresh_token', response.refresh_token);
        localStorage.setItem('id_token', response.id_token);
        
        this.toastService.showSuccess('Sucesso', 'Cadastro realizado com sucesso!');
        
        if (response.is_first_login) {
          this.router.navigate(['/complete-profile']);
        } else {
          this.router.navigate(['/home']);
        }
      },
      error: (error) => {
        console.error('Erro no cadastro:', error);
        const errorMessage = error.error?.message || error.error || 'Erro ao realizar cadastro';
        this.toastService.showError('Erro', errorMessage);
        this.loading = false;
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
