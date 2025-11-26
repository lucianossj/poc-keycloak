# Frontend - Angular Application

Este é o frontend da POC de integração com Keycloak.

## Requisitos

- Node.js 20+
- npm 10+

## Tecnologias

- Angular 17
- TypeScript
- SCSS

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

## Estrutura

```
frontend/
├── src/
│   ├── app/
│   │   ├── login/                  # Componente de login
│   │   │   ├── login.component.ts
│   │   │   ├── login.component.html
│   │   │   └── login.component.scss
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app.config.ts
│   │   └── app.routes.ts
│   ├── styles.scss                 # Estilos globais
│   └── index.html
├── angular.json
├── package.json
└── README.md
```

## Funcionalidades

- [x] Tela de login moderna e responsiva
- [x] Botão de login com Google (visual)
- [x] Botão de login com Instagram (visual)
- [ ] Integração funcional com Keycloak
- [ ] Autenticação OAuth2 com Google
- [ ] Autenticação OAuth2 com Instagram
