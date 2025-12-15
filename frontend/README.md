# 🎨 Frontend - Angular 17 SPA

Single Page Application moderna para autenticação completa (senha + Google OAuth) com UI/UX responsiva.

## 📋 Requisitos

- Node.js 20+
- npm 10+

## 🛠️ Tecnologias

- **Angular 17** com standalone components
- **TypeScript 5.0+** com strict mode
- **RxJS** para programação reativa
- **SCSS** com variáveis e mixins
- **Modern CSS** com animations e flexbox
- **HTTP Interceptors** para tratamento de erros

## Como executar

### Instalar dependências

```bash
npm install
```

### Iniciar servidor de desenvolvimento

```bash
ng serve
```

A aplicação estará disponível em: http://localhost:4200

## Build

```bash
ng build
```

Os artefatos de build serão armazenados no diretório `dist/`.

## 📁 Estrutura

```
frontend/src/app/
├── login/
│   ├── login.component.ts          # Login (senha + Google)
│   ├── login.component.html        # Template moderno
│   ├── login.component.scss        # Estilos responsivos
│   └── service/
│       └── login.service.ts        # AuthService OAuth
├── register/
│   ├── register.component.ts       # Cadastro com senha
│   ├── register.component.html     # Formulário cadastro
│   └── register.component.scss     # Estilos cadastro
├── complete-profile/
│   ├── complete-profile.component.ts   # Completar perfil (CPF)
│   ├── complete-profile.component.html # Formulário CPF
│   └── complete-profile.component.scss # Estilos perfil
├── auth-callback/
│   └── auth-callback.component.ts  # Handler OAuth callback
├── logout-callback/
│   └── logout-callback.component.ts # Handler logout callback
├── home/
│   ├── home.component.ts           # Dashboard pós-login
│   ├── home.component.html         # Template dashboard
│   └── home.component.scss         # Estilos dashboard
├── shared/
│   ├── services/
│   │   ├── toast.service.ts        # Sistema de notificações
│   │   └── direct-auth.service.ts  # Login com senha
│   ├── components/
│   │   └── toast/
│   │       ├── toast.component.ts  # Componente toast
│   │       └── toast.component.scss # Estilos toast
│   └── interceptors/
│       └── error.interceptor.ts    # Interceptor HTTP errors
├── auth.guard.ts                   # Guard rotas protegidas
├── login.guard.ts                  # Guard redirecionamento logado
├── app.component.ts                # Root component
├── app.config.ts                   # Configuração da aplicação
└── app.routes.ts                   # Roteamento da SPA
```

## ✨ Funcionalidades Implementadas

### ✅ Autenticação Dupla
- [x] **Login com senha** (CPF/email + senha)
- [x] **Login com Google OAuth**
- [x] **Cadastro de usuários** com senha
- [x] **Authorization Code Flow** completo
- [x] **JWT Token Storage** no localStorage
- [x] **Auto-redirect** após login
- [x] **Token validation** e expiração
- [x] **Logout** com revogação de tokens

### ✅ Gerenciamento de Perfil
- [x] **Complete Profile** para primeiro login social
- [x] **Formulário de CPF** obrigatório
- [x] **Validação de CPF** e data de nascimento
- [x] **Account Linking** automático
- [x] **Atualização de dados** do cliente

### ✅ UI/UX Moderna
- [x] **Tela de login** com senha + Google
- [x] **Tela de cadastro** responsiva
- [x] **Complete profile** elegante
- [x] **Loading states** durante operações
- [x] **Toast notifications** para feedback
- [x] **Error handling** com mensagens amigáveis
- [x] **Smooth animations** e transições
- [x] **Mobile-first design**

### ✅ Navegação
- [x] **Route Guards** implementados
- [x] **Protected routes** funcionais
- [x] **Auto-redirect** se já autenticado
- [x] **Callback handlers** para OAuth e logout
- [x] **Home dashboard** pós-login

## 🗺️ Rotas da Aplicação

| Rota | Componente | Proteção | Descrição |
|------|-----------|----------|-----------|
| `/` | Home | AuthGuard | Dashboard (redireciona /home) |
| `/home` | Home | AuthGuard | Dashboard pós-login |
| `/login` | Login | LoginGuard | Tela de login (senha + Google) |
| `/register` | Register | LoginGuard | Cadastro com senha |
| `/complete-profile` | CompleteProfile | AuthGuard | Completar perfil (CPF) |
| `/auth/callback` | AuthCallback | - | Handler OAuth callback |
| `/logout-callback` | LogoutCallback | - | Handler logout callback |

## 🎨 Serviços Principais

### LoginService (`login.service.ts`)
Service para autenticação OAuth Google:
```typescript
- getGoogleAuthUrl(): void              // Inicia fluxo OAuth
- handleCallback(code): void            // Processa callback
- isAuthenticated(): boolean            // Verifica autenticação
- logout(): void                        // Logout completo
```

### DirectAuthService (`direct-auth.service.ts`)
Service para login com senha:
```typescript
- login(username, password): Observable  // Login com credenciais
- register(data): Observable             // Cadastro com senha
```

### ToastService (`toast.service.ts`)
Sistema de notificações:
```typescript
- showSuccess(title, message)            // Toast de sucesso
- showError(title, message)              // Toast de erro
- showWarning(title, message)            // Toast de aviso
- showInfo(title, message)               // Toast informativo
```

## 🛡️ Guards Implementados

### AuthGuard
Protege rotas que exigem autenticação:
- Verifica se usuário está autenticado (token no localStorage)
- Redireciona para `/login` se não autenticado
- Usado em: `/home`, `/complete-profile`

### LoginGuard
Protege rotas de login quando já autenticado:
- Verifica se usuário JÁ está autenticado
- Redireciona para `/home` se já logado
- Usado em: `/login`, `/register`

## 🚀 Scripts Disponíveis

```bash
npm start          # Servidor de desenvolvimento
npm run build      # Build para produção
npm run watch      # Build em modo watch
npm test           # Executar testes
npm run lint       # Linting do código
```
