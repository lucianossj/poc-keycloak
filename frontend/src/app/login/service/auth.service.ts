import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, Observable, switchMap, tap, throwError } from 'rxjs';
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

    public registerUser(registerData: any): Observable<any> {
        try {
            this.validateRegisterData(registerData);

            return this.http.post('http://localhost:8081/auth/register', registerData).pipe(
                tap((response: any) => {
                    this.setDataOnStorage(response);
                    this.toastService.showSuccess('Sucesso', 'Cadastro realizado com sucesso!');
                    this.validateFirstLoginAndNavigate(response);
                }),
                catchError((error) => {
                    console.error('Erro no cadastro:', error);
                    const errorMessage = error.error?.message || error.error || 'Erro ao realizar cadastro';
                    this.toastService.showError('Erro', errorMessage);
                    return throwError(() => error);
                }));
        } catch (error) {
            return throwError(() => error);
        }
        
    }

    public getGoogleAuthUrl(): void {
        this.http.get<{ authUrl: string }>(`${this.backendUrl}/auth/url`)
            .subscribe({
                next: response => this.redirectToGoogleAuth(response),
                error: error => this.handleGetUrlError(error)
            });
    }

    private redirectToGoogleAuth(response: any): void {
        window.location.href = response.authUrl;
    }

    private handleGetUrlError(error: any): void {
        console.error('Erro ao iniciar login:', error);
        this.toastService.showError(
            'Erro no Login',
            'Não foi possível iniciar o processo de login. Tente novamente.'
        );
    }

    public async handleCallback(code: string): Promise<void> {
        try {
            this.exchangeCodeForTokens(code)
                .pipe(
                    map(response => this.setDataOnStorage(response)),
                    catchError(error => this.handleCallbackError(error, 'Erro na Autenticação', 'Falha ao processar os dados de autenticação.'))
                )
                .subscribe({
                    next: () => this.handleCallbackSuccess(),
                    error: error => this.handleCallbackError(error, 'Erro no Login', 'Não foi possível obter suas informações. Tente fazer login novamente.')
                });
        } catch (error) {
            this.toastService.showError(
                'Erro na Autenticação',
                'Ocorreu um erro inesperado durante o login. Tente novamente.'
            );
            this.router.navigate(['/login'], { queryParams: { error: 'auth_failed' } });
        }
    }

    public loginWithPassword(email: string, password: string): Observable<any> {
        this.validateEmailAndPassword(email, password);
        const loginData = this.buildLoginData(email, password);

        return this.http.post('http://localhost:8081/auth/login', loginData).pipe(
            tap((response: any) => {
                this.setDataOnStorage(response);
                this.toastService.showSuccess('Sucesso', 'Login realizado com sucesso!');
                this.validateFirstLoginAndNavigate(response);
            }),
            catchError((error) => {
                console.error('Erro no login:', error);
                const errorMessage = error.error?.message || error.error || 'Erro ao realizar login';
                this.toastService.showError('Erro', errorMessage);
                return throwError(() => error);
            })
        );
    }

    public logout(): void {
        const idToken = localStorage.getItem('id_token');
        const accessToken = localStorage.getItem('access_token');

        if (idToken) {
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
        } else {
            console.log('No id_token available, performing local logout only');
            this.handleLogoutSuccess(`/login`);
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

    public getStoredUserInfo(): any {
        const userInfoStr = localStorage.getItem('user_info');
        return userInfoStr ? JSON.parse(userInfoStr) : null;
    }

    public clearFirstLoginFlag(): void {
        localStorage.removeItem('is_first_login');
    }

    private validateRegisterData(registerData: any): void {
        if (!registerData.name || !registerData.email || !registerData.document || !registerData.birthDate || !registerData.password) {
        this.toastService.showError('Erro', 'Preencha todos os campos');
        throw new Error('Dados incompletos');
        }

        if (registerData.password !== registerData.confirmPassword) {
            console.log('Senhas não coincidem', registerData.password, registerData.confirmPassword);
        this.toastService.showError('Erro', 'As senhas não coincidem');
        throw new Error('Senhas não coincidem');
        }

        if (registerData.password.length < 6) {
        this.toastService.showError('Erro', 'A senha deve ter no mínimo 6 caracteres');
        throw new Error('Senha muito curta');
        }
    }

    private handleLogoutSuccess(logoutUrl?: string): void {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('id_token');
        localStorage.removeItem('user_info');
        localStorage.removeItem('is_first_login');

        if (logoutUrl) {
            window.location.href = logoutUrl;
        } else {
            this.router.navigate(['/login']);
        }
    }

    private validateEmailAndPassword(email: string, password: string): void {
        if (!email || !password) {
            this.toastService.showError('Erro', 'Preencha e-mail e senha');
            return;
        }
    }
    
    private buildLoginData(email: string, password: string): any {
        return {
            email: email,
            password: password
        };
    }

    private validateFirstLoginAndNavigate(response: any): void {
        if (response.is_first_login) {
            this.router.navigate(['/complete-profile']);
        } else {
            this.router.navigate(['/home']);
        }
    }

    private exchangeCodeForTokens(code: string): Observable<any> {
        return this.http.post<any>(`${this.backendUrl}/auth/token`, { code: code });
    }

    private setDataOnStorage(response: any): any {
        localStorage.setItem('access_token', response.access_token);
        localStorage.setItem('refresh_token', response.refresh_token);
        
        if (response.id_token) {
            localStorage.setItem('id_token', response.id_token);
        }
        
        if (response.user_info) {
            localStorage.setItem('user_info', JSON.stringify(response.user_info));
        }
        
        if (response.is_first_login !== undefined) {
            localStorage.setItem('is_first_login', String(response.is_first_login));
        }
        
        return response;
    }

    private getUserInfo(accessToken: string): Observable<any> {
        return this.http.get<any>(`${this.backendUrl}/auth/user-info`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });
    }

    private handleCallbackSuccess(): void {
        const isFirstLogin = localStorage.getItem('is_first_login') === 'true';
        
        if (isFirstLogin) {
            this.toastService.showInfo(
                'Bem-vindo!',
                'Complete seu cadastro para continuar'
            );
            this.router.navigate(['/complete-profile']);
        } else {
            this.toastService.showSuccess(
                'Login Realizado',
                'Bem-vindo de volta!'
            );
            this.router.navigate(['/home']);
        }
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

}