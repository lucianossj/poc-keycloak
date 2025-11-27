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
  username: string = '';
  password: string = '';
  grantType: string = '';
  rememberMe: boolean = false;

  constructor(private authService: AuthService) { }

  onSubmit(): void {
    console.log('Login submitted');
  }

  loginWithGoogle(): void {
    this.grantType = 'GOOGLE';
    this.authService.login(this.username, this.password, this.grantType);
  }

  loginWithInstagram(): void {
    this.grantType = 'INSTAGRAM';
    this.authService.login(this.username, this.password, this.grantType);
  }
}
