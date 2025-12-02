# 🎨 Frontend - Angular 17 SPA

Single Page Application moderna para autenticação Google OAuth com UI/UX responsiva.

## 📋 Requisitos

- Node.js 20+
- npm 10+

## 🛠️ Tecnologias

- **Angular 17** com standalone components
- **TypeScript 5.0+** com strict mode
- **RxJS** para programação reativa
- **SCSS** com variáveis e mixins
- **Modern CSS** com animations e flexbox

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
│   ├── login.component.ts          # Componente de login
│   ├── login.component.html        # Template com Google button
│   ├── login.component.scss        # Estilos modernos e responsivos
│   └── service/
│       └── login.service.ts        # AuthService completo
├── auth-callback/
│   └── auth-callback.component.ts  # Handler do OAuth callback
├── home/
│   └── home.component.ts          # Dashboard pós-login
├── shared/
│   ├── services/
│   │   └── toast.service.ts       # Sistema de notificações
│   └── components/
│       └── toast.component.ts     # Componente de toast
├── auth.guard.ts                  # Guard para rotas protegidas
├── login.guard.ts                 # Guard para redirecionamento
├── app.component.ts               # Root component
├── app.config.ts                  # Configuração da aplicação
└── app.routes.ts                  # Roteamento da SPA
```

## ✨ Funcionalidades Implementadas

### ✅ Autenticação
- [x] **Google OAuth Login** funcional
- [x] **Authorization Code Flow** completo
- [x] **JWT Token Storage** no localStorage
- [x] **Auto-redirect** após login bem-sucedido
- [x] **Token validation** e expiração
- [x] **Logout** com revogação de tokens

### ✅ UI/UX Moderna
- [x] **Tela de login** responsiva e elegante
- [x] **Loading states** durante autenticação
- [x] **Toast notifications** para feedback
- [x] **Error handling** com mensagens amigáveis
- [x] **Smooth animations** e transições
- [x] **Mobile-first design**

### ✅ Navegação
- [x] **Route Guards** implementados
- [x] **Protected routes** funcionais
- [x] **Auto-redirect** se já autenticado
- [x] **Callback handler** para OAuth
- [x] **Home dashboard** pós-login

## 🎨 Componentes Principais

### AuthService (`login.service.ts`)
```typescript
- getGoogleAuthUrl(): void          // Inicia fluxo OAuth
- handleCallback(code): void        // Processa callback
- isAuthenticated(): boolean        // Verifica autenticação
- logout(): void                    // Logout completo
- getUserInfo(token): Observable    // Dados do usuário
```

### ToastService (`toast.service.ts`)
```typescript
- showSuccess(title, message)       // Toast de sucesso
- showError(title, message)         // Toast de erro
- showWarning(title, message)       // Toast de aviso
- showInfo(title, message)          // Toast informativo
```

## 🚀 Scripts Disponíveis

```bash
npm start          # Servidor de desenvolvimento
npm run build      # Build para produção
npm run watch      # Build em modo watch
npm test           # Executar testes
npm run lint       # Linting do código
```
