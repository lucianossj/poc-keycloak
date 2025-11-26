# Arch - Infrastructure Configuration

Este diretório contém a configuração de infraestrutura para rodar o Keycloak e PostgreSQL localmente.

## Requisitos

- Docker
- Docker Compose

## Como usar

### Iniciar os serviços

```bash
./scripts/start.sh
```

Ou diretamente:

```bash
docker-compose up -d
```

### Parar os serviços

```bash
./scripts/stop.sh
```

### Resetar todos os dados

```bash
./scripts/reset.sh
```

## Acessos

### Keycloak
- URL: http://localhost:8080
- Admin Console: http://localhost:8080/admin
- Usuário: `admin`
- Senha: `admin`

### PostgreSQL
- Host: localhost
- Porta: 5432
- Database: `keycloak`
- Usuário: `keycloak`
- Senha: `keycloak_password`

## Estrutura

```
arch/
├── docker-compose.yml    # Configuração dos containers
├── README.md             # Esta documentação
└── scripts/
    ├── init-db.sql       # Script de inicialização do banco
    ├── start.sh          # Script para iniciar serviços
    ├── stop.sh           # Script para parar serviços
    └── reset.sh          # Script para resetar dados
```
