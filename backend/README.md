# 🚀 Backend - Spring Boot 3 API

API REST para autenticação completa (senha + Google OAuth) via Keycloak com gerenciamento de usuários e MongoDB.

## 📋 Requisitos

- Java 21
- Gradle 8+
- MongoDB (rodando via Docker)
- Keycloak (rodando via Docker)

## 🛠️ Tecnologias

- **Java 21** com Spring Boot 3.2.0
- **Spring Security** para CORS e endpoints
- **Spring Web** para REST APIs
- **Spring Data MongoDB** para persistência
- **Keycloak Admin REST API** para gerenciamento de usuários
- **OAuth 2.0** (Authorization Code + Direct Access Grant)
- **Lombok** para redução de boilerplate
- **Clean Code** com Single Responsibility Principle aplicado

## Como executar

```bash
./gradlew bootRun
```

O servidor estará disponível em: http://localhost:8081

## 📁 Estrutura

```
backend/src/main/java/com/example/backend/
├── config/
│   ├── SecurityConfig.java          # Spring Security + CORS
│   ├── RestTemplateConfig.java      # Bean RestTemplate
│   └── KeycloakProperties.java      # Properties do Keycloak
├── controller/
│   ├── AuthorizationController.java # Endpoints autenticação
│   └── CustomerController.java      # Endpoints clientes
├── service/
│   ├── AuthorizationService.java    # Service autenticação (13 métodos refatorados)
│   ├── CustomerService.java         # Service clientes (16 métodos refatorados)
│   ├── KeycloakAdminService.java    # Admin REST API (30+ métodos, SRP aplicado)
│   ├── KeycloakHttpClient.java      # Protocol endpoints (code exchange, userInfo)
│   ├── KeycloakUrlService.java      # Construção de URLs OAuth
│   └── KeycloakLogoutService.java   # Lógica de logout
├── model/
│   ├── Customer.java                # Entity MongoDB
│   ├── LoginResponse.java           # DTO resposta
│   └── dto/
│       ├── LoginRequestDTO.java     # DTO login com senha
│       └── RegisterRequestDTO.java  # DTO cadastro
└── util/
    └── UrlUtils.java                # Utilitários para URLs
```

## 🔍 Endpoints Implementados

### Autenticação (`/auth/*`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/auth/url` | Gerar URL de autenticação Google (OAuth) |
| `POST` | `/auth/register` | Cadastrar novo usuário com senha (cria no Keycloak + MongoDB) |
| `POST` | `/auth/login` | Login com CPF/email + senha (Direct Access Grant) |
| `POST` | `/auth/token` | Trocar authorization code por tokens JWT |
| `POST` | `/auth/logout` | Logout e revogação de tokens |

### Clientes (`/customers/*`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/customers` | Listar todos os clientes |
| `GET` | `/customers/{id}` | Buscar cliente por ID |
| `GET` | `/customers/by-email/{email}` | Buscar cliente por email |
| `GET` | `/customers/by-keycloak/{keycloakUserId}` | Buscar cliente por keycloakUserId |
| `POST` | `/customers` | Criar novo cliente (usado internamente) |
| `PUT` | `/customers/{id}` | Atualizar dados do cliente (CPF, nome, data nascimento) |

## ⚙️ Configurações (`application.properties`)

```properties
# Servidor
server.port=8081

# Keycloak Configuration  
keycloak.url=http://localhost:8080
keycloak.realm=poc-ecommerce
keycloak.client-id=poc-ecommerce-app
# keycloak.client-secret= (não necessário para public client)
keycloak.redirect-uri=http://localhost:4200/auth/callback
keycloak.post-logout-redirect-uri=http://localhost:4200/login
keycloak.idp-hint=google
keycloak.admin-username=admin
keycloak.admin-password=admin

# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=fake_cartao
spring.data.mongodb.username=admin
spring.data.mongodb.password=admin123
spring.data.mongodb.authentication-database=admin
```

## ✅ Funcionalidades Implementadas

- [x] **OAuth 2.0 Authorization Code Flow** para Google Login
- [x] **Direct Access Grant** para login com senha
- [x] **Cadastro de usuários** com senha (Keycloak + MongoDB)
- [x] **CPF como username** para TODOS os usuários
- [x] **Account Linking** automático (vincula Google a conta existente por email)
- [x] **Username Update** para federated users (delete → create → relink)
- [x] **Keycloak Admin REST API** integration completa
- [x] **MongoDB** para persistência de clientes
- [x] **JWT Token Management** (access, refresh, id)  
- [x] **CORS** configurado para desenvolvimento
- [x] **Error handling** centralizado
- [x] **Clean Code** com SRP (métodos pequenos e focados)
- [x] **Logout com revogação** de tokens

## 🏗️ Arquitetura e Design Patterns

### Clean Code Aplicado

Todos os services foram refatorados seguindo **Single Responsibility Principle**:

- **AuthorizationService**: 13 métodos pequenos (≤10 linhas cada)
  - Extraído record `UserIdentity` como value object
  - Métodos focados: `shouldUpdateUsername()`, `linkGoogleAccountToExistingCustomer()`, etc.

- **CustomerService**: 16 métodos pequenos
  - Validações extraídas: `validateDocumentNotInUse()`, `validateDocumentFormat()`
  - Operações atômicas: `updateCustomerBasicInfo()`, `syncWithKeycloak()`

- **KeycloakAdminService**: 30+ métodos pequenos
  - Responsabilidades separadas: token management, user CRUD, attributes, federated identity
  - Pattern delete-create-relink para username updates de federated users
  - Sem emojis, logs mínimos (só errors), código auto-documentado

### Separação de Responsabilidades

- **KeycloakAdminService**: Admin REST API (gerenciamento de usuários)
- **KeycloakHttpClient**: Protocol endpoints (autenticação OAuth)
- **KeycloakUrlService**: Construção de URLs
- **KeycloakLogoutService**: Lógica de logout

Cada service tem uma responsabilidade clara e bem definida.
