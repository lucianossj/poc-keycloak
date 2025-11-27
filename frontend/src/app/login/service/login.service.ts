import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, map, Observable, switchMap } from 'rxjs';

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
    private keycloakUrl = 'http://localhost:8080';
    private backendUrl = 'http://localhost:8081';
    private realm = 'poc-ecommerce';

    constructor(
        private router: Router,
        private http: HttpClient
    ) {
        // Verificar se já tem token válido
        this.checkExistingToken();
    }

    public login(username:string, password: string, grantType: string): void {
        const request = this.buildGoogleLoginRequest(username, password, grantType);
        this.http.post<{ authUrl: string }>(`${this.backendUrl}/auth/login`, request)
            .subscribe(response => {
                if (response.authUrl) {
                    window.location.href = response.authUrl;
                }
            });
    }

    private buildGoogleLoginRequest(username:string, password: string, grantType: string): any {
        return {
            grantType: grantType
        };
    }

    async handleCallback(code: string): Promise<void> {
        try {
            this.exchangeCodeForTokens(code)
                .pipe(
                    map(response => {
                        console.log('Tokens recebidos:', response);

                        localStorage.setItem('access_token', response.access_token);
                        localStorage.setItem('refresh_token', response.refresh_token);
                        localStorage.setItem('id_token', response.id_token);

                        return response;
                    }),
                    switchMap(response => this.getUserInfo(response.access_token)),
                    catchError(error => {
                        console.error('Erro ao processar callback:', error);
                        throw error;
                    })
                )
                .subscribe(
                    response => this.router.navigate(['/home']),
                    error => console.error('Erro ao obter informações do usuário:', error)
                );

        } catch (error) {
            console.error('Erro no callback:', error);
            this.router.navigate(['/login'], { queryParams: { error: 'auth_failed' } });
        }
    }

    // TODO: Use RxJS and refactors
    logout(): void {
        const idToken = localStorage.getItem('id_token');
        const postLogoutRedirectUri = 'http://localhost:4200/login';
        const logoutUrl = `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/logout?id_token_hint=${idToken}&post_logout_redirect_uri=${encodeURIComponent(postLogoutRedirectUri)}`;
        // Limpar tokens locais
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('id_token');
        window.location.href = logoutUrl;
    }

    /**
     * Verificar se está autenticado
     */
    isAuthenticated(): boolean {
        const token = localStorage.getItem('access_token');
        if (!token) return false;

        // Verificar se token não expirou
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const now = Math.floor(Date.now() / 1000);
            return payload.exp > now;
        } catch {
            return false;
        }
    }

    /**
     * Obter token para requisições
     */
    getToken(): string | null {
        return localStorage.getItem('access_token');
    }

    /**
     * Trocar authorization code por tokens
     */
    private exchangeCodeForTokens(code: string): Observable<any> {
        return this.http.post<any>(`${this.backendUrl}/auth/token`, { code: code });        
    }

    private getUserInfo(accessToken: string): Observable<any> {
        console.log('Buscando informações do usuário com token:', accessToken);
        return this.http.get<any>(`${this.backendUrl}/auth/user-info`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });
    }

    /**
     * Verificar token existente na inicialização
     */
    private async checkExistingToken(): Promise<void> {
        if (this.isAuthenticated()) {
            try {
                const token = this.getToken()!;
                this.getUserInfo(token).subscribe(
                    user => {
                    },
                    error => {
                        console.error('Erro ao obter informações do usuário:', error);
                        this.logout();
                    }
                );
            } catch (error) {
                console.error('Token inválido:', error);
                this.logout();
            }
        }
    }
}