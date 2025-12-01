import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, map, Observable, switchMap, throwError } from 'rxjs';
import { GrantType } from '../../shared/enums/grant-type.enum';
import { ToastService } from '../../shared/services/toast.service';

export interface User {
    sub: string;
    name: string;
    email: string;
    preferred_username: string;
    given_name: string;
    family_name: string;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly backendUrl = 'http://localhost:8081';

    constructor(
        private router: Router,
        private http: HttpClient,
        private toastService: ToastService
    ) {
        this.checkExistingToken();
    }

    public login(username: string, password: string, grantType: GrantType): void {
        const request = this.buildGoogleLoginRequest(username, password, grantType);

        this.http.post<{ authUrl: string }>(`${this.backendUrl}/auth/login`, request)
            .subscribe(
                response => this.handleLoginSuccess(response),
                error => this.handleLoginError(error)
            );
    }

    private handleLoginSuccess(response: any): void {
        const isSocialLogin = this.grantTypeIsSocialLogin(response.grantType);

        if (isSocialLogin) {
            this.handleSocialLoginSuccess(response);
            return;
        }

        return;
    }

    private handleLoginError(error: any): void {
        console.error('Erro ao iniciar login:', error);
        this.toastService.showError(
            'Erro no Login',
            'Não foi possível iniciar o processo de login. Tente novamente.'
        );
    }

    private handleSocialLoginSuccess(response: any): void {
        if (response.authUrl) {
            window.location.href = response.authUrl;
        }
    }

    private buildGoogleLoginRequest(username: string, password: string, grantType: GrantType): any {
        return {
            username: username,
            password: password,
            grantType: grantType
        };
    }

    public async handleCallback(code: string): Promise<void> {
        try {
            this.exchangeCodeForTokens(code)
                .pipe(
                    map(response => this.setTokensOnStorageAndReturn(response)),
                    switchMap(response => this.getUserInfo(response?.access_token)),
                    catchError(error => this.handleCallbackError(error, 'Erro na Autenticação', 'Falha ao processar os dados de autenticação.'))
                )
                .subscribe(
                    () => this.handleCallbackSuccess(),
                    error => this.handleCallbackError(error, 'Erro no Login', 'Não foi possível obter suas informações. Tente fazer login novamente.')
                );
        } catch (error) {
            this.toastService.showError(
                'Erro na Autenticação',
                'Ocorreu um erro inesperado durante o login. Tente novamente.'
            );
            this.router.navigate(['/login'], { queryParams: { error: 'auth_failed' } });
        }
    }

    public logout(): void {
        const idToken = localStorage.getItem('id_token');

        this.http.post<any>(`${this.backendUrl}/auth/logout`, {
            id_token: idToken
        }).subscribe({
            next: (response) => {
                if (response.logoutUrl) {
                    this.handleLogoutSuccess(response.logoutUrl);
                } else {
                    this.handleLogoutSuccess(`/login`);
                }
            },
            error: () => {
                this.toastService.showError(
                    'Erro no Logout',
                    'Ocorreu um erro ao realizar logout. Limpando dados locais...'
                );
                this.handleLogoutSuccess(`/login`);
            }
        });
    }

    private handleLogoutSuccess(logoutUrl?: string): void {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('id_token');

        if (logoutUrl) {
            window.location.href = logoutUrl;
        } else {
            this.router.navigate(['/login']);
        }
    }

    public isAuthenticated(): boolean {
        const token = localStorage.getItem('access_token');
        if (!token) return false;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const now = Math.floor(Date.now() / 1000);
            return payload.exp > now;
        } catch {
            return false;
        }
    }

    public getToken(): string | null {
        return localStorage.getItem('access_token');
    }

    private exchangeCodeForTokens(code: string): Observable<any> {
        return this.http.post<any>(`${this.backendUrl}/auth/token`, { code: code });
    }

    private setTokensOnStorageAndReturn(response: any): any {
        localStorage.setItem('access_token', response.access_token);    // Autorizar requisições
        localStorage.setItem('refresh_token', response.refresh_token);  // Renovar token
        localStorage.setItem('id_token', response.id_token);            // Identidade do usuário
        return response;
    }

    private getUserInfo(accessToken: string): Observable<any> {
        console.log('Buscando informações do usuário com token:', accessToken);
        return this.http.get<any>(`${this.backendUrl}/auth/user-info`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });
    }

    private handleCallbackSuccess(): void {
        this.toastService.showSuccess(
            'Login Realizado',
            'Bem-vindo! Login realizado com sucesso.'
        );
        this.router.navigate(['/home']);
    }

    private handleCallbackError(error: any, title: string, message: string): Observable<never> {
        this.toastService.showError(
            title,
            message
        );
        return throwError(() => error);
    }

    private async checkExistingToken(): Promise<void> {
        if (this.isAuthenticated()) {
            try {
                const token = this.getToken()!;
                this.getUserInfo(token).subscribe();
            } catch (error) {
                this.toastService.showWarning(
                    'Sessão Expirada',
                    'Sua sessão expirou. Faça login novamente.'
                );
                this.logout();
            }
        }
    }

    private grantTypeIsSocialLogin(grantType: GrantType): boolean {
        return grantType == GrantType.GOOGLE || grantType == GrantType.INSTAGRAM;
    }
}