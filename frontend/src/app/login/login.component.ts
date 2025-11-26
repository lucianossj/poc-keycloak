import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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

  onSubmit(): void {
    console.log('Login submitted:', { email: this.email, rememberMe: this.rememberMe });
    // TODO: Implement authentication logic with Keycloak
  }

  loginWithGoogle(): void {
    console.log('Login with Google clicked');
    // TODO: Implement Google OAuth integration via Keycloak
  }

  loginWithInstagram(): void {
    console.log('Login with Instagram clicked');
    // TODO: Implement Instagram OAuth integration via Keycloak
  }
}
