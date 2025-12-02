# 🚀 Backend - Spring Boot 3 API

API REST para autenticação Google OAuth via Keycloak com arquitetura SOLID.

## 📋 Requisitos

- Java 21
- Maven 3.9+

## 🛠️ Tecnologias

- **Java 21** com Spring Boot 3
- **Spring Security** para CORS e endpoints
- **Spring Web** para REST APIs
- **Keycloak Integration** customizada
- **Lombok** para redução de boilerplate
- **Arquitetura SOLID** com services especializados

## Como executar

```bash
./mvnw spring-boot:run
```

O servidor estará disponível em: http://localhost:8081

## 📁 Estrutura

```
backend/src/main/java/com/example/backend/
├── config/
│   ├── SecurityConfig.java       # Spring Security + CORS
│   ├── CorsConfig.java          # Configuração CORS detalhada
│   └── KeycloakProperties.java  # Properties do Keycloak
├── controller/
│   └── AuthorizationController.java # Endpoints de autenticação
├── service/
│   ├── AuthorizationService.java    # Service principal (Facade)
│   ├── KeycloakUrlService.java     # Construção de URLs OAuth
│   ├── KeycloakHttpClient.java     # Requisições HTTP para Keycloak
│   └── KeycloakLogoutService.java  # Lógica de logout
├── integration/
│   └── KeycloakIntegration.java    # Integração com Keycloak (Facade)
├── model/
│   ├── GrantType.java              # Enum de tipos de autenticação
│   └── LoginResponse.java          # DTO de resposta
├── util/
│   └── UrlUtils.java               # Utilitários para URLs
└── BackendApplication.java         # Classe principal Spring Boot
```

## 🔍 Endpoints Implementados

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/auth/url` | Gerar URL de autenticação Google |
| `POST` | `/auth/token` | Trocar authorization code por tokens |
| `GET` | `/auth/user-info` | Obter dados do usuário autenticado |
| `POST` | `/auth/logout` | Logout e revogação de tokens |

## ⚙️ Configurações (`application.properties`)

```properties
# Servidor
server.port=8081

# Keycloak Configuration  
keycloak.auth-server-url=http://localhost:8080
keycloak.realm=master
keycloak.client-id=account
keycloak.client-secret=
keycloak.redirect-uri=http://localhost:4200/auth-callback
keycloak.post-logout-redirect-uri=http://localhost:4200/login
keycloak.idp-hint=google
```

## ✅ Funcionalidades Implementadas

- [x] **OAuth 2.0 Authorization Code Flow**
- [x] **Integração completa com Keycloak**
- [x] **JWT Token Management** (access, refresh, id)  
- [x] **CORS configurado** para desenvolvimento
- [x] **Error handling** centralizado
- [x] **Clean Architecture** seguindo SOLID
- [x] **Logout com revogação** de tokens
