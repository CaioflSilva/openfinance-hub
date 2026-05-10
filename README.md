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

## ✨ Funcionalidades

- 🔐 **Autenticação segura** com Spring Security + JWT
- 🏦 **Múltiplas contas bancárias** por usuário (corrente, poupança, investimento)
- ⚡ **Processamento assíncrono** de transações com RabbitMQ
- 🗄️ **Cache inteligente** de saldos e consultas com Redis
- 🏷️ **Categorização automática** de gastos (alimentação, transporte, saúde...)
- 📊 **Dashboard consolidado** com saldo total e resumo por categoria
- 🔔 **Alertas de risco** para gastos acima do padrão
- 💸 **Simulação de PIX** com processamento em fila

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

### 2. Suba a infraestrutura

```bash
docker-compose up -d
```

Isso vai iniciar:
- PostgreSQL na porta `5432`
- RabbitMQ na porta `5672` (Management UI: `15672`)
- Redis na porta `6379`

### 3. Rode a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

---

## 📋 Endpoints

### Auth
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/auth/register` | Cadastro de usuário |
| POST | `/api/auth/login` | Login e geração de token JWT |

### Contas Bancárias
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/accounts` | Conectar nova conta bancária |
| GET | `/api/accounts` | Listar todas as contas do usuário |
| GET | `/api/accounts/{id}` | Detalhes de uma conta |
| DELETE | `/api/accounts/{id}` | Remover conta |

### Transações
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/transactions` | Registrar transação (vai para fila) |
| GET | `/api/transactions` | Listar transações com filtros |
| POST | `/api/transactions/pix` | Simular transação PIX |

### Dashboard
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/dashboard` | Saldo consolidado e resumo |
| GET | `/api/dashboard/summary` | Gastos por categoria |
| GET | `/api/dashboard/alerts` | Alertas de risco financeiro |

---

## 🗺️ Roadmap

- [x] Setup do projeto
- [ ] Configuração Docker Compose
- [ ] Configuração Spring Security + JWT
- [ ] Models e Repositories
- [ ] Autenticação (register/login)
- [ ] CRUD de contas bancárias
- [ ] Processamento de transações com RabbitMQ
- [ ] Cache com Redis
- [ ] Dashboard consolidado
- [ ] Alertas de risco
- [ ] Simulação PIX
- [ ] Testes unitários e de integração

---

## 👨‍💻 Autor

**Caio Filipe**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-blue?style=for-the-badge&logo=linkedin)](https://linkedin.com/in/caiofilipe)
[![GitHub](https://img.shields.io/badge/GitHub-black?style=for-the-badge&logo=github)](https://github.com/CaioflSilva)

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.