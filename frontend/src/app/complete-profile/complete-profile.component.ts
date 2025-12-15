import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CompleteProfileService } from './service/complete-profile.service';
import { ToastService } from '../shared/services/toast.service';
import { AuthService } from '../login/service/login.service';

@Component({
  selector: 'app-complete-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './complete-profile.component.html',
  styleUrls: ['./complete-profile.component.scss']
})
export class CompleteProfileComponent implements OnInit {
  document: string = '';
  birthDate: string = '';
  loading: boolean = false;
  keycloakUserId: string = '';
  userInfo: any;

  constructor(
    private router: Router,
    private completeProfileService: CompleteProfileService,
    private toastService: ToastService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userInfo = this.authService.getStoredUserInfo();
    
    if (!this.userInfo) {
      this.toastService.showError('Erro', 'Informações do usuário não encontradas');
      this.router.navigate(['/login']);
      return;
    }
    
    this.keycloakUserId = this.userInfo.sub;
  }

  formatDocument(event: any): void {
    let value = event.target.value.replace(/\D/g, '');
    
    if (value.length <= 11) {
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    }
    
    this.document = value;
  }

  isFormValid(): boolean {
    const cleanDocument = this.document.replace(/\D/g, '');
    return cleanDocument.length === 11 && this.birthDate.length > 0;
  }

  async onSubmit(): Promise<void> {
    if (!this.isFormValid()) {
      this.toastService.showWarning('Atenção', 'Preencha todos os campos corretamente');
      return;
    }

    this.loading = true;

    try {
      // Envia CPF e data de nascimento para o backend
      // O backend irá:
      // 1. Salvar no MongoDB
      // 2. Atualizar atributos customizados no Keycloak
      // 3. SE for login social (Google): Atualizar username de email para CPF
      await this.completeProfileService.updateCustomerInfo(
        this.keycloakUserId,
        this.document,
        this.birthDate
      );

      this.toastService.showSuccess('Sucesso', 'Cadastro completado com sucesso!');
      
      this.authService.clearFirstLoginFlag();
      
      setTimeout(() => {
        this.router.navigate(['/home']);
      }, 1000);

    } catch (error: any) {
      this.toastService.showError(
        'Erro ao completar cadastro',
        error.error?.message || 'Tente novamente mais tarde'
      );
      this.loading = false;
    }
  }

  skip(): void {
    this.authService.clearFirstLoginFlag();
    this.toastService.showInfo('Pulado', 'Você pode completar seu cadastro depois no perfil');
    this.router.navigate(['/home']);
  }
}
