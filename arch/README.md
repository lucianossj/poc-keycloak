# Arch - Infrastructure Configuration

Este diretório contém a configuração de infraestrutura para rodar o Keycloak e PostgreSQL localmente.

## Requisitos

- Docker
- Docker Compose

## Configuração

1. Copie o arquivo `.env.example` para `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edite o arquivo `.env` com suas credenciais seguras.

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
- Credenciais: Confira o arquivo `.env` ou `docker-compose.yml`

### PostgreSQL
- Host: localhost
- Porta: 5432
- Credenciais: Confira o arquivo `.env` ou `docker-compose.yml`

## Estrutura

```
arch/
├── docker-compose.yml    # Configuração dos containers
├── .env.example          # Exemplo de variáveis de ambiente
├── README.md             # Esta documentação
└── scripts/
    ├── init-db.sql       # Script de inicialização do banco
    ├── start.sh          # Script para iniciar serviços
    ├── stop.sh           # Script para parar serviços
    └── reset.sh          # Script para resetar dados
```
