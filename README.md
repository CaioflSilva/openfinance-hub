# 🏦 OpenFinance Hub API

![CI](https://github.com/CaioflSilva/openfinance-hub/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.14-green?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange?style=for-the-badge&logo=rabbitmq)
![Redis](https://img.shields.io/badge/Redis-7-red?style=for-the-badge&logo=redis)
![Docker](https://img.shields.io/badge/Docker-compose-blue?style=for-the-badge&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

> API REST que simula um hub de Open Finance — consolida múltiplas contas bancárias, processa transações de forma assíncrona, categoriza gastos automaticamente e emite alertas de risco financeiro em tempo real.

---

## 📌 Sobre o Projeto

O Open Finance é uma das maiores tendências do mercado financeiro brasileiro. Bancos como Itaú, Nubank e BTG precisam de sistemas capazes de:

- Consolidar dados de **múltiplas contas** em tempo real
- Processar **milhares de transações** de forma assíncrona sem travar
- Responder consultas em **milissegundos** com cache inteligente
- Garantir **segurança em cada camada** da aplicação

O **OpenFinance Hub** foi desenvolvido para simular exatamente esse cenário, aplicando arquitetura e tecnologias usadas em sistemas financeiros reais.

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────┐
│                    CLIENT (REST)                     │
└─────────────────────┬───────────────────────────────┘
                      │ HTTP
┌─────────────────────▼───────────────────────────────┐
│              SPRING SECURITY + JWT                   │
│                  (Auth Layer)                        │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                  CONTROLLERS                         │
│     /auth  /accounts  /transactions  /dashboard      │
└──────┬──────────────┬──────────────┬────────────────┘
       │              │              │
┌──────▼──────┐ ┌─────▼──────┐ ┌────▼────────────────┐
│   SERVICE   │ │   REDIS    │ │     RABBITMQ         │
│   LAYER     │ │  (Cache)   │ │  (Async Processing)  │
└──────┬──────┘ └────────────┘ └────────────────────┬─┘
       │                                             │
┌──────▼─────────────────────────────────────────────▼─┐
│                    PostgreSQL                          │
│         users │ bank_accounts │ transactions           │
└───────────────────────────────────────────────────────┘
```

---

## 🚀 Tech Stack

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/Docker_Compose-2496ED?style=flat-square&logo=docker&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?style=flat-square&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-5-C5B4E3?style=flat-square)
![Testcontainers](https://img.shields.io/badge/Testcontainers-black?style=flat-square&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=flat-square&logo=githubactions&logoColor=white)

---

## ✨ Features

- 🔐 **JWT stateless auth** — register + login com BCrypt, token assinado via HMAC-SHA256
- 🏦 **Gestão de contas bancárias** — CRUD com ownership validation (403 se não for o dono)
- ⚡ **Processamento assíncrono** — transações publicadas em fila RabbitMQ e consumidas em background
- 🗄️ **Cache Redis** — dashboard com TTL de 5 minutos via `@Cacheable` e `@CacheEvict` na criação de transações
- 📊 **Dashboard consolidado** — resumo de gastos por categoria em tempo real
- 🛡️ **Tratamento global de erros** — 404 ResourceNotFoundException, 403 ForbiddenException
- 🧪 **15 testes automatizados** — 3 suítes unitárias (Mockito) + 2 de integração (Testcontainers + PostgreSQL real)
- ⚙️ **CI/CD** — GitHub Actions executa os testes em todo push/PR para `main`
- 🔑 **Secrets via .env** — nenhuma credencial hardcoded; `spring-dotenv` carrega o `.env` automaticamente

---

## 🛠️ Stack e Decisões Técnicas

| Tecnologia | Versão | Por quê? |
|---|---|---|
| Java | 21 | LTS mais recente, Virtual Threads para alta concorrência |
| Spring Boot | 3.5.14 | Padrão do mercado bancário brasileiro |
| Spring Security + JWT | - | Autenticação stateless, padrão para APIs REST |
| PostgreSQL | 15 | Banco relacional robusto para dados financeiros |
| RabbitMQ | 3 | Processamento assíncrono de transações, evita gargalos |
| Redis | 7 | Cache de saldos e consultas frequentes em memória |
| Docker | - | Ambiente reproduzível, padrão em pipelines CI/CD |
| Lombok | - | Redução de boilerplate sem perder legibilidade |

### Por que RabbitMQ e não processamento síncrono?

Em sistemas financeiros, processar uma transação de forma síncrona significa que o cliente fica esperando até o banco de dados confirmar. Com RabbitMQ, a transação é publicada numa fila instantaneamente, o cliente recebe confirmação imediata, e o processamento acontece em background. Isso é exatamente como o PIX funciona.

### Por que Redis?

Um banco consulta saldo milhões de vezes por dia. Ir ao PostgreSQL toda vez seria lento e custoso. O Redis guarda os dados mais acessados em memória, respondendo em menos de 1ms.

---

## 📁 Estrutura do Projeto

```
src/
└── main/
    └── java/com/caiofilipe/openfinancehub/
        ├── config/          # Configurações (Security, Redis, RabbitMQ)
        ├── controller/      # Endpoints REST
        ├── service/         # Regras de negócio
        ├── repository/      # Interfaces JPA
        ├── model/           # Entidades do banco de dados
        ├── dto/
        │   ├── request/     # Objetos de entrada
        │   └── response/    # Objetos de saída
        ├── messaging/
        │   ├── producer/    # Publicadores de mensagens
        │   └── consumer/    # Consumidores de filas
        ├── exception/       # Handlers globais de erro
        └── security/        # JWT Filter e configurações
```

---

## 🚀 Como Rodar Localmente

### Pré-requisitos

- Java 21+
- Docker e Docker Compose

### 1. Clone o repositório

```bash
git clone https://github.com/CaioflSilva/openfinance-hub.git
cd openfinance-hub
```

### 2. Configure as variáveis de ambiente

```bash
cp .env.example .env
```

Edite o `.env` com os valores reais:

| Variável | Descrição | Exemplo |
|---|---|---|
| `DB_PASSWORD` | Senha do PostgreSQL | `postgres` |
| `RABBITMQ_PASSWORD` | Senha do RabbitMQ | `guest` |
| `JWT_SECRET` | Secret HMAC-SHA256 em base64 (256 bits) | veja abaixo |
| `JWT_EXPIRATION` | Tempo de expiração do token em ms | `86400000` (24h) |

Para gerar um `JWT_SECRET` seguro:
```bash
# Linux/macOS
openssl rand -base64 32

# PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { [byte](Get-Random -Max 256) }))
```

> **Nunca commite o `.env`** — ele já está no `.gitignore`.

### 3. Suba a infraestrutura

```bash
docker-compose up -d
```

Isso vai iniciar:
- PostgreSQL na porta `5432`
- RabbitMQ na porta `5672` (Management UI: `15672`)
- Redis na porta `6379`

### 4. Rode a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

---

## 📡 API Endpoints

### Auth
| Método | Rota | Descrição | JWT |
|---|---|---|---|
| `POST` | `/api/auth/register` | Registrar novo usuário | Não |
| `POST` | `/api/auth/login` | Autenticar e obter token JWT | Não |

### Contas Bancárias
| Método | Rota | Descrição | JWT |
|---|---|---|---|
| `POST` | `/api/bank-accounts` | Criar conta bancária | Sim |
| `GET` | `/api/bank-accounts` | Listar contas do usuário autenticado | Sim |
| `GET` | `/api/bank-accounts/{id}` | Detalhes de uma conta (ownership check) | Sim |
| `DELETE` | `/api/bank-accounts/{id}` | Remover conta (ownership check) | Sim |

### Transações
| Método | Rota | Descrição | JWT |
|---|---|---|---|
| `POST` | `/api/transactions` | Criar transação (publica em fila RabbitMQ) | Sim |
| `GET` | `/api/transactions` | Listar transações do usuário autenticado | Sim |
| `GET` | `/api/transactions/account/{accountId}` | Transações de uma conta específica | Sim |
| `GET` | `/api/transactions/{id}` | Detalhes de uma transação | Sim |

### Dashboard
| Método | Rota | Descrição | JWT |
|---|---|---|---|
| `GET` | `/api/dashboard/summary` | Resumo consolidado (Redis cache 5 min) | Sim |

---


## 👨‍💻 Autor

**Caio Filipe**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-blue?style=for-the-badge&logo=linkedin)](https://linkedin.com/in/caiofilipe)
[![GitHub](https://img.shields.io/badge/GitHub-black?style=for-the-badge&logo=github)](https://github.com/CaioflSilva)

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.