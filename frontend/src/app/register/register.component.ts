import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../shared/services/toast.service';
import { AuthService } from '../login/service/auth.service';

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
    private router: Router,
    private authService: AuthService
  ) {}

  public formatCPF(event: any): void {
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

  public registerUser(): void {
    this.loading = true;
    const registerData = this.buildRegisterData();

    this.authService.registerUser(registerData).subscribe({
      next: () => {
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  private buildRegisterData(): any {
    return {
      name: this.name,
      email: this.email,
      document: this.document.replace(/\D/g, ''),
      birthDate: this.birthDate,
      password: this.password,
      confirmPassword: this.confirmPassword
    };
  }
}
