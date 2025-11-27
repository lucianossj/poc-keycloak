import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from './service/login.service';

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
  rememberMe: boolean = false;

  constructor(private authService: AuthService) { }

  onSubmit(): void {
    console.log('Login submitted');
    // TODO: Implement authentication logic with Keycloak
  }

  loginWithGoogle(): void {
    console.log('Login with Google clicked');
    this.authService.loginWithGoogle();
  }

  loginWithInstagram(): void {
    console.log('Login with Instagram clicked');
    // TODO: Implement Instagram OAuth integration via Keycloak
  }
}
