import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';

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
    private realm = 'poc-ecommerce';
    private clientId = 'poc-ecommerce-app';
    private redirectUri = 'http://localhost:4200/auth/callback';

    private currentUserSubject = new BehaviorSubject<User | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(private router: Router) {
        // Verificar se já tem token válido
        this.checkExistingToken();
    }

    /**
     * Fazer login direto com Google via Keycloak
     */
    loginWithGoogle(): void {
        const authUrl = `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/auth?` +
            `client_id=${this.clientId}&` +
            `redirect_uri=${encodeURIComponent(this.redirectUri)}&` +
            `response_type=code&` +
            `scope=openid%20profile%20email&` +
            `kc_idp_hint=google`;  // 🎯 Força login direto com Google

        // Redirecionar para Keycloak
        window.location.href = authUrl;
    }

    // http://localhost:8080/realms/poc-ecommerce/protocol/openid-connect/auth?%20client_id=poc-ecommerce-app&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fcallback&response_type=code&scope=openid%20profile%20email&kc_idp_hint=google

    /**
     * Login normal (mostra tela do Keycloak com opções)
     */
    login(): void {
        const authUrl = `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/auth?` +
            `client_id=${this.clientId}&` +
            `redirect_uri=${encodeURIComponent(this.redirectUri)}&` +
            `response_type=code&` +
            `scope=openid%20profile%20email`;

        window.location.href = authUrl;
    }

    /**
     * Processar callback do Keycloak
     */
    async handleCallback(code: string): Promise<void> {
        try {
            // Trocar authorization code por tokens
            const tokenResponse = await this.exchangeCodeForTokens(code);
            console.log('Token response:', tokenResponse);

            // Armazenar tokens
            localStorage.setItem('access_token', tokenResponse.access_token);
            localStorage.setItem('refresh_token', tokenResponse.refresh_token);
            localStorage.setItem('id_token', tokenResponse.id_token);

            // Buscar informações do usuário
            const userInfo = await this.getUserInfo(tokenResponse.access_token);

            // Atualizar estado
            this.currentUserSubject.next(userInfo);

            // Redirecionar para home
            this.router.navigate(['/home']);

        } catch (error) {
            console.error('Erro no callback:', error);
            this.router.navigate(['/login'], { queryParams: { error: 'auth_failed' } });
        }
    }

    /**
     * Logout
     */
    logout(): void {
        const idToken = localStorage.getItem('id_token');
        const postLogoutRedirectUri = 'http://localhost:4200/login';
        const logoutUrl = `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/logout?id_token_hint=${idToken}&post_logout_redirect_uri=${encodeURIComponent(postLogoutRedirectUri)}`;
        // Limpar tokens locais
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('id_token');
        this.currentUserSubject.next(null);
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
     * Obter informações do usuário atual
     */
    getCurrentUser(): User | null {
        return this.currentUserSubject.value;
    }

    /**
     * Trocar authorization code por tokens
     */
    private async exchangeCodeForTokens(code: string): Promise<any> {
        const tokenUrl = `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/token`;

        const body = new URLSearchParams({
            grant_type: 'authorization_code',
            client_id: this.clientId,
            code: code,
            redirect_uri: this.redirectUri
        });

        const response = await fetch(tokenUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: body.toString()
        });

        if (!response.ok) {
            throw new Error('Failed to exchange code for tokens');
        }

        return response.json();
    }

    /**
     * Buscar informações do usuário
     */
    private async getUserInfo(accessToken: string): Promise<User> {
        const userInfoUrl = `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/userinfo`;

        const response = await fetch(userInfoUrl, {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to get user info');
        }

        return response.json();
    }

    /**
     * Verificar token existente na inicialização
     */
    private async checkExistingToken(): Promise<void> {
        if (this.isAuthenticated()) {
            try {
                const token = this.getToken()!;
                const userInfo = await this.getUserInfo(token);
                this.currentUserSubject.next(userInfo);
            } catch (error) {
                console.error('Token inválido:', error);
                this.logout();
            }
        }
    }
}