# Nota Técnica — Decisões, Trade-offs e Melhorias Futuras

## 1. Visão Geral da Arquitetura Atual

O sistema Hotel Fácil é composto por duas aplicações independentes:

- **Frontend**: Angular 19 (SPA), hospedado no Vercel
- **Backend**: Spring Boot 3 + MySQL, hospedado no Railway
- **Comunicação**: REST síncrono via HTTP, autenticação JWT stateless

A separação frontend/backend com deploy independente permite que cada camada evolua e escale de forma autônoma, o que foi uma decisão deliberada desde o início.

---

## 2. Decisões Técnicas e Trade-offs

### 2.1 REST Síncrono

**Decisão**: toda comunicação entre frontend e backend é REST síncrona.

**Justificativa**: simplicidade de implementação e depuração para o escopo atual (CRUD de hotéis, quartos e reservas).

**Trade-off**: em cenários de alta carga, chamadas síncronas bloqueiam threads e aumentam a latência percebida. Operações como envio de e-mail de confirmação, geração de relatórios ou notificações de status ficam acopladas ao ciclo de request/response.

---

### 2.2 Autenticação JWT Stateless

**Decisão**: tokens JWT armazenados no cliente, sem sessão no servidor.

**Justificativa**: elimina estado de sessão no backend, facilitando escalabilidade horizontal.

**Trade-off**: revogação imediata de tokens é complexa — uma vez emitido, o token é válido até expirar. Solução atual: tempo de expiração curto + refresh token.

---

### 2.3 Banco de Dados Único (MySQL)

**Decisão**: um único banco relacional para toda a aplicação.

**Justificativa**: consistência transacional nativa, modelo de dados relacional adequado para reservas com integridade referencial (hotel → quarto → reserva).

**Trade-off**: ponto único de falha. Sob alta carga de leitura, o banco se torna gargalo. Não há separação entre leituras e escritas.

---

### 2.4 Deploy Monolítico no Backend

**Decisão**: toda a lógica de negócio em um único serviço Spring Boot.

**Justificativa**: velocidade de desenvolvimento, menor complexidade operacional para o escopo atual.

**Trade-off**: acoplamento entre domínios (hotéis, quartos, reservas, autenticação). Uma falha em qualquer módulo afeta toda a aplicação.

---

## 3. Melhorias Futuras

### 3.1 Comunicação Assíncrona com Message Broker

**Problema que resolve**: operações como confirmação de reserva, notificação por e-mail e geração de voucher hoje bloqueiam a thread do request.

**Proposta**: introduzir um message broker (RabbitMQ ou Apache Kafka) para desacoplar o processamento dessas operações.

```
Cliente → API → publica evento "ReservaCriada" → fila → serviço de notificação
                                                        → serviço de voucher
                                                        → serviço de relatórios
```

**Ganho**: o request de criação de reserva retorna imediatamente ao cliente; o restante acontece em background. Resilência a picos de carga.

---

### 3.2 API Gateway

**Problema que resolve**: o frontend consome diretamente o backend. Em uma evolução para microsserviços, haveria múltiplos endpoints para gerenciar, com autenticação duplicada em cada serviço.

**Proposta**: introduzir um API Gateway (Spring Cloud Gateway ou Kong) na frente de todos os serviços.

**Responsabilidades do gateway**:
- Autenticação e validação de JWT centralizada
- Rate limiting por IP/usuário
- Roteamento dinâmico para os microsserviços
- Observabilidade centralizada (logs, métricas, tracing)

**Trade-off**: adiciona um hop de rede e um ponto de configuração a mais. Complexidade operacional aumenta.

---

### 3.3 Escalabilidade Horizontal do Backend

**Problema que resolve**: instância única no Railway não suporta picos de tráfego sem degradação.

**Proposta**: tornar o serviço completamente stateless (já está, graças ao JWT) e rodar múltiplas réplicas atrás de um load balancer.

**Pré-requisitos**:
- Sessões já são stateless (JWT) — ok
- Cache distribuído (ver seção 3.4) para evitar que cada réplica consulte o banco separadamente
- Migrações de banco (Flyway) executadas apenas uma vez, não a cada instância

**Stack sugerida**: Railway com múltiplas réplicas ou migração para Kubernetes (GKE/EKS) com HPA (Horizontal Pod Autoscaler).

---

### 3.4 Camada de Cache

**Problema que resolve**: leituras repetidas de dados estáveis (lista de hotéis, detalhes de quartos) sobrecarregam o banco desnecessariamente.

**Proposta**: Redis como cache distribuído.

**Estratégia por recurso**:

| Recurso          | Estratégia      | TTL sugerido |
|------------------|-----------------|--------------|
| Lista de hotéis  | Cache-aside     | 10 minutos   |
| Detalhes do quarto | Cache-aside   | 5 minutos    |
| Disponibilidade  | Sem cache       | —            |
| Dados do usuário | Cache por token | Duração JWT  |

**Importante**: disponibilidade de quartos **não deve ser cacheada** — dados desatualizados causariam double-booking (exatamente o incidente descrito no INCIDENT_ANALYSIS).

---

### 3.5 Resolução Definitiva do Race Condition (Double-Booking)

**Problema**: conforme documentado no INCIDENT_ANALYSIS, a verificação de disponibilidade e a criação da reserva são operações separadas (TOCTOU), permitindo double-booking sob concorrência.

**Proposta de solução em camadas**:

1. **Lock pessimista** no banco: `SELECT ... FOR UPDATE` na consulta de disponibilidade, garantindo que apenas uma transação por vez acesse o quarto em determinado período
2. **Unique constraint composta**: `(room_id, check_in, check_out)` no banco como última linha de defesa
3. **Chave de idempotência**: o cliente envia um `idempotency-key` no header; o backend rejeita duplicatas dentro de uma janela de tempo

---

### 3.6 Decomposição em Microsserviços (Longo Prazo)

**Motivação**: à medida que o sistema cresce, o monólito se torna difícil de manter e escalar seletivamente.

**Decomposição sugerida**:

```
hotel-service       → CRUD de hotéis e quartos
reservation-service → criação e cancelamento de reservas
auth-service        → login, registro, refresh token
notification-service → e-mails, SMS, webhooks (assíncrono)
```

**Trade-off**: complexidade operacional exponencialmente maior (service discovery, distributed tracing, eventual consistency). Só vale se a equipe e o tráfego justificarem.

---

## 4. Resumo dos Trade-offs

| Decisão Atual         | Benefício                        | Limitação                              | Evolução Sugerida              |
|-----------------------|----------------------------------|----------------------------------------|-------------------------------|
| REST síncrono         | Simplicidade                     | Acoplamento temporal                   | Message broker para eventos    |
| JWT stateless         | Escalabilidade horizontal fácil  | Revogação complexa                     | Blacklist com Redis            |
| Banco único           | Consistência transacional        | Gargalo de leitura, ponto único falha  | Read replica + cache Redis     |
| Monólito              | Velocidade de desenvolvimento    | Acoplamento entre domínios             | Microsserviços gradual         |
| Sem cache             | Dados sempre frescos             | Carga desnecessária no banco           | Redis cache-aside              |
| Verificação TOCTOU    | Implementação simples            | Double-booking sob concorrência        | Lock pessimista + constraint   |
