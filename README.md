# POC para integração de login com Google e Instagram via Keycloak

Este projeto é uma prova de conceito (POC) para integração de login social (Google e Instagram) utilizando Keycloak como Identity Provider.

## Estrutura do Projeto

```
poc-keycloak/
├── arch/           # Infraestrutura (Docker, Keycloak, PostgreSQL)
├── backend/        # API Spring Boot 3 com Java 21
├── frontend/       # Aplicação Angular 17
└── README.md       # Esta documentação
```

## Pré-requisitos

- Docker e Docker Compose
- Java 21
- Node.js 20+
- Maven 3.9+

## Como iniciar

### 1. Iniciar a infraestrutura (Keycloak + PostgreSQL)

```bash
cd arch
./scripts/start.sh
```

Acesse o Keycloak em: http://localhost:8080
- Usuário: `admin`
- Senha: `admin`

### 2. Iniciar o Backend

```bash
cd backend
./mvnw spring-boot:run
```

O backend estará disponível em: http://localhost:8081

### 3. Iniciar o Frontend

```bash
cd frontend
npm install
ng serve
```

A aplicação estará disponível em: http://localhost:4200

## Funcionalidades

- [x] Infraestrutura Docker com Keycloak e PostgreSQL
- [x] Backend Spring Boot 3 com Java 21
- [x] Frontend Angular com tela de login moderna
- [x] Botões visuais para login com Google e Instagram
- [ ] Integração funcional com Keycloak
- [ ] OAuth2 com Google
- [ ] OAuth2 com Instagram

## Documentação

- [Arch - Infraestrutura](./arch/README.md)
- [Backend - Spring Boot](./backend/README.md)
- [Frontend - Angular](./frontend/README.md)