# Backend - Spring Boot 3 Application

Este é o backend da POC de integração com Keycloak.

## Requisitos

- Java 21
- Maven 3.9+

## Tecnologias

- Java 21
- Spring Boot 3.2.0
- Spring Security
- Spring OAuth2 Resource Server

## Como executar

```bash
./mvnw spring-boot:run
```

O servidor estará disponível em: http://localhost:8081

## Estrutura

```
backend/
├── pom.xml                                    # Configuração Maven
├── README.md                                  # Esta documentação
└── src/
    ├── main/
    │   ├── java/com/example/backend/
    │   │   └── BackendApplication.java        # Classe principal
    │   └── resources/
    │       └── application.properties         # Configurações
    └── test/
        └── java/com/example/backend/
            └── BackendApplicationTests.java   # Testes
```

## Próximos passos

- [ ] Configurar integração com Keycloak
- [ ] Adicionar endpoints protegidos
- [ ] Configurar CORS para o frontend
