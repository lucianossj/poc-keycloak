# Keycloak Identity Provider - Configurações do Google para E-commerce

Este documento explica as configurações do Identity Provider do Google no Keycloak especificamente para **login em aplicações de e-commerce**, focando na experiência do usuário e facilidade de uso.

## Índice

1. [Configurações Essenciais](#configurações-essenciais)
2. [Configurações Avançadas](#configurações-avançadas)
3. [Configuração Recomendada para E-commerce](#configuração-recomendada-para-e-commerce)
4. [Boas Práticas para E-commerce](#boas-práticas-para-e-commerce)

---

## Configurações Essenciais

### Display Order (Ordem de Exibição)

**Descrição:** Define a ordem em que este Identity Provider aparece na tela de login quando múltiplos provedores estão configurados.

**Recomendação para E-commerce:** Use `0` ou `1` para fazer o Google aparecer primeiro, pois é uma das formas mais populares e confiáveis de login para consumidores.

**Exemplo:**
```
Display Order: 0
```
**Não se aplica porque não utilizaremos a UI do Keycloak.**

---

### Hosted Domain (Domínio Hospedado)

**Descrição:** Restringe o login apenas a usuários de um domínio específico do Google Workspace.

**Recomendação para E-commerce:** **Deixe vazio** para permitir que qualquer pessoa com conta Google (@gmail.com, @outlook.com, etc.) possa fazer login no seu e-commerce.

**Exemplo:**
```
Hosted Domain: [vazio]
```
**Resultado:** Qualquer cliente com conta Google pode fazer login, maximizando a base de usuários potenciais.

---

### Use userIp Param (Usar Parâmetro userIp)

**Status Recomendado:** Off

**Descrição:** Inclui o IP do usuário nas requisições para a API do Google.

**Por que Off para E-commerce:**
- Foco na simplicidade e velocidade do login
- Não há necessidade de controle avançado de quotas
- Melhor privacidade para os clientes

---

### Request Refresh Token (Solicitar Token de Atualização)

**Status Recomendado:** Off

**Descrição:** Solicita um refresh token do Google para renovar tokens automaticamente.

**Por que Off para E-commerce:**
- E-commerce usa Google apenas para autenticação/login
- Não precisamos acessar APIs do Google após o login
- Reduz complexidade e melhora segurança
- Login mais rápido e simples

---

## Configurações Avançadas

### Scopes (Escopos)

**Descrição:** Define quais permissões serão solicitadas ao usuário durante o processo de autenticação OAuth.

**Recomendação para E-commerce:** Use apenas os escopos essenciais para não assustar o cliente com muitas permissões.

**Escopos Recomendados para E-commerce:**
```
Scopes: openid email
```

**Explicação dos escopos:**
- `openid`: Identificação básica (obrigatório)
- `email`: Endereço de email (para comunicação e identificação)

**Resultado:** Cliente vê uma tela de permissão simples e não intrusiva, aumentando a taxa de conversão.

---

### Store Tokens (Armazenar Tokens)

**Status Recomendado:** Off

**Descrição:** Determina se o Keycloak deve armazenar os tokens OAuth recebidos do Google.

**Por que Off para E-commerce:**
- Não precisamos fazer chamadas para APIs do Google após o login
- Reduz dados armazenados no banco
- Maior segurança (menos tokens sensíveis armazenados)
- Foco apenas na autenticação

---

### Accepts prompt=none forward from client

**Status Recomendado:** On (para SPAs)

**Descrição:** Permite tentativas de autenticação silenciosa.

**Por que On para E-commerce:**
- Melhora experiência em SPAs (React, Angular, Vue)
- Cliente pode navegar entre páginas sem perder sessão
- Verificação silenciosa se usuário ainda está logado
- Evita interrupções desnecessárias no fluxo de compra

---

### Disable User Info (Desabilitar Informações do Usuário)

**Status Recomendado:** Off

**Descrição:** Pula chamada para endpoint `/userinfo` do Google.

**Por que Off para E-commerce:**
- Garantimos informações completas do cliente
- Nome e email atualizados
- Melhor personalização da experiência
- Dados confiáveis para perfil do cliente

---

### Trust Email (Confiar no Email)

**Status Recomendado:** On

**Descrição:** Confia que o email do Google já foi verificado.

**Por que On para E-commerce:**
- Google já verifica emails rigorosamente
- Experiência mais fluida, sem etapas extras
- Cliente não precisa confirmar email novamente
- Reduz abandono no processo de cadastro

---

### Account Linking Only (Apenas Vinculação de Conta)

**Status Recomendado:** Off

**Descrição:** Permite apenas vincular contas existentes, não criar novas.

**Por que Off para E-commerce:**
- Permite cadastro automático de novos clientes
- Facilita aquisição de novos usuários
- Experiência de onboarding mais fluida
- Maximiza conversão de visitantes em clientes

---

### Hide on Login Page (Ocultar na Página de Login)

**Status Recomendado:** Off

**Descrição:** Esconde o botão do Google na página de login padrão.

**Não se aplica porque não utilizaremos a UI do Keycloak.**


---

### First Login Flow (Fluxo do Primeiro Login)

**Valor Recomendado:** first broker login

**Descrição:** Define o fluxo quando usuário faz login pela primeira vez.

**Por que usar padrão para E-commerce:**
- Criação automática e rápida de conta
- Sem etapas extras que possam causar abandono
- Experiência otimizada para conversão
- Cliente entra direto no sistema após autorização

---
### Post Login Flow (Fluxo Pós-Login)

**Valor Recomendado:** None (ou personalizado se necessário)

**Descrição:** Fluxo adicional após login bem-sucedido.

**Opções para E-commerce:**
- **None:** Cliente vai direto para onde estava
- **Custom:** Para coleta de dados específicos (endereço, telefone, preferências)

**Exemplo de uso personalizado:**
```
Post Login Flow: collect-shipping-preferences
```

---

### Sync Mode (Modo de Sincronização)

**Valor Recomendado:** Import

**Descrição:** Como sincronizar informações entre Google e Keycloak.

**Por que Import para E-commerce:**
- Dados iniciais vêm do Google na primeira vez
- Permite que cliente edite dados no perfil posteriormente
- Não sobrescreve informações que cliente adicionou (endereço, telefone, etc.)
- Flexibilidade para dados específicos do e-commerce

---

## Configuração Recomendada para E-commerce

Baseado nas melhores práticas para e-commerce, aqui está a configuração otimizada para maximizar conversão e experiência do usuário:

### 🎯 Configuração Ideal para E-commerce

```yaml
# Configurações Básicas
Display Order: 0                    # Google como primeira opção
Hosted Domain: [vazio]              # Aceita qualquer conta Google
Use userIp Param: Off               # Simplicidade e privacidade
Request Refresh Token: Off          # Apenas autenticação, sem APIs

# Configurações Avançadas
Scopes: "openid profile email"      # Mínimo necessário
Store Tokens: Off                   # Sem armazenamento de tokens
Accepts prompt=none: On             # Para SPAs e melhor UX
Disable User Info: Off              # Dados completos do usuário
Trust Email: On                     # Confia na verificação do Google
Account Linking Only: Off           # Permite novos cadastros
Hide on Login Page: Off             # Visível na tela de login
First Login Flow: first broker login # Cadastro automático e rápido
Post Login Flow: None               # Sem etapas extras (ou personalizado)
Sync Mode: Import                   # Flexibilidade para dados do e-commerce
```

### 💡 Benefícios desta Configuração

**Para o Cliente:**
- ✅ Login rápido com apenas 1-2 cliques
- ✅ Não precisa verificar email novamente
- ✅ Cadastro automático sem formulários longos
- ✅ Funciona com qualquer conta Google (@gmail.com, @hotmail.com, etc.) sem necessidade do Google Workspace
- ✅ Experiência fluida em SPAs

**Para o E-commerce:**
- ✅ Maior taxa de conversão (menos abandono)
- ✅ Mais cadastros de usuários
- ✅ Dados confiáveis (nome, email)
- ✅ Reduz custo de suporte (menos problemas de login)
- ✅ Configuração simples e segura
