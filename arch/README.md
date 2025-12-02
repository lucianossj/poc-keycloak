# 🏗️ Infraestrutura - Keycloak & PostgreSQL

Configuração de infraestrutura para autenticação Google OAuth via Keycloak com PostgreSQL.

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

### 🔐 Keycloak
- **URL**: http://localhost:8080
- **Admin Console**: http://localhost:8080/admin  
- **Credenciais**: `admin` / `admin`
- **Realm**: `master` (configurado para Google OAuth)

### 🐘 PostgreSQL  
- **Host**: localhost
- **Porta**: 5432
- **Database**: `keycloak`
- **Credenciais**: Definidas em `docker-compose.yml`

## 🔧 Configuração Google OAuth

Após subir a infraestrutura, configure no Keycloak Admin Console:

1. **Identity Providers** → Add provider → **Google**
2. **Client ID** e **Client Secret** do Google Cloud Console  
3. **Redirect URI**: `http://localhost:8080/realms/master/broker/google/endpoint`

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
