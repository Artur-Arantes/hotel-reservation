# 🏨 Hotel Fácil — Backend

API REST para gerenciamento de hotéis, quartos e reservas. Desenvolvida com Spring Boot e Java 21, com autenticação JWT, controle de disponibilidade por período e documentação via Swagger.

---

## 🔗 Links de Produção

| Recurso | URL |
|---|---|
| API (base) | https://hotel-reservation-production-e49c.up.railway.app |
| Swagger UI | https://hotel-reservation-production-e49c.up.railway.app/swagger-ui/index.html |
| Frontend | https://hotel-reservation-front-xi.vercel.app |

---

## 📋 Funcionalidades

- **Autenticação** — registro, login e refresh token com JWT stateless
- **Hotéis** — listagem paginada, busca por ID, criação, atualização e remoção
- **Quartos** — busca por ID, verificação de disponibilidade por período, criação e remoção
- **Reservas** — criação, listagem paginada, busca por hóspede, busca por ID e cancelamento
- **Tratamento de erros** — handler global com respostas padronizadas e HTTP status corretos

---

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4 |
| Segurança | Spring Security + JWT (Auth0 java-jwt 4.4) |
| Banco de dados | MySQL 8 |
| Migrações | Flyway |
| Persistência | Spring Data JPA + Hibernate |
| Documentação | SpringDoc OpenAPI / Swagger UI 2.8 |
| Utilitários | Lombok |
| Build | Maven |
| Deploy | Railway |

---

## 🚀 Rodando localmente

### Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker (recomendado para o banco) **ou** MySQL 8 instalado localmente

---

### Opção 1 — Banco via Docker (recomendado)

O projeto inclui um `docker-compose.yml` que sobe o MySQL com as configurações já prontas.

```bash
docker-compose up -d
```

Isso cria automaticamente o banco `hotel_reservation` na porta `3306`.  
Configure as variáveis de ambiente apontando para esse banco:

```bash
export DB_URL=jdbc:mysql://localhost:3306/hotel_reservation
export DB_USER=root
export DB_PASSWORD=123456
export SECRET_KEY=minha-chave-secreta-local
```

---

### Opção 2 — MySQL local

Se preferir usar um MySQL já instalado na máquina, crie o banco manualmente:

```sql
CREATE DATABASE hotel_reservation;
```

E configure as variáveis com as credenciais do seu ambiente:

```bash
export DB_URL=jdbc:mysql://localhost:3306/hotel_reservation
export DB_USER=seu_usuario
export DB_PASSWORD=sua_senha
export SECRET_KEY=minha-chave-secreta-local
```

> Você também pode configurar as variáveis diretamente na sua IDE como run environment variables, sem precisar exportar no terminal.

---

### Executando a aplicação

```bash
mvn spring-boot:run
```

A aplicação sobe na porta `8080`.

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

> O Flyway executa as migrações automaticamente na primeira inicialização — as tabelas e dados iniciais são criados sem nenhuma configuração manual.

---

## 📡 Endpoints

### Auth — `/api/auth`

| Método | Rota | Descrição | Requer token |
|---|---|---|---|
| POST | `/register` | Cria um novo usuário | Não |
| POST | `/login` | Autentica e retorna access + refresh token | Não |
| POST | `/refresh` | Renova o access token | Não |

### Hotéis — `/api/hotels`

| Método | Rota | Descrição | Requer token |
|---|---|---|---|
| GET | `/` | Lista todos os hotéis (paginado) | Sim |
| GET | `/{id}` | Busca hotel por ID | Sim |
| POST | `/` | Cria um novo hotel | Sim |
| PUT | `/{id}` | Atualiza dados de um hotel | Sim |
| DELETE | `/{id}` | Remove um hotel | Sim |

### Quartos — `/api/rooms`

| Método | Rota | Descrição | Requer token |
|---|---|---|---|
| GET | `/{id}` | Busca quarto por ID | Sim |
| POST | `/availability` | Lista quartos disponíveis por período | Sim |
| POST | `/` | Cria um novo quarto | Sim |
| DELETE | `/{id}` | Remove um quarto | Sim |

### Reservas — `/api/reservations`

| Método | Rota | Descrição | Requer token |
|---|---|---|---|
| GET | `/` | Lista todas as reservas (paginado) | Sim |
| GET | `/{id}` | Busca reserva por ID | Sim |
| GET | `/guest` | Busca reservas por nome do hóspede | Sim |
| POST | `/` | Cria uma nova reserva | Sim |
| PATCH | `/{id}/cancel` | Cancela uma reserva | Sim |

> Endpoints protegidos exigem o header `Authorization: Bearer <token>`.  
> O token é obtido via `POST /api/auth/login`.

---

## 🐳 Rodando com Docker

Se preferir rodar a aplicação em um container (conectando a um banco externo ou local):

```bash
# Build da imagem
docker build -t hotel-facil-api .
```

**Linux:**

```bash
docker run --network host \
  -e DB_URL=jdbc:mysql://127.0.0.1:3306/hotel_reservation \
  -e DB_USER=root \
  -e DB_PASSWORD=123456 \
  -e SECRET_KEY=minha-chave-secreta \
  hotel-facil-api
```

**Mac / Windows (Docker Desktop):**

```bash
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/hotel_reservation \
  -e DB_USER=root \
  -e DB_PASSWORD=123456 \
  -e SECRET_KEY=minha-chave-secreta \
  hotel-facil-api
```

> No Linux, `--network host` faz o container compartilhar a rede da máquina, então `127.0.0.1` resolve para o MySQL local normalmente. Nesse modo o `-p` não é necessário — a porta `8080` já fica exposta diretamente.

---

## 🧪 Testes

```bash
mvn test
```

---

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/br/com/hotel/reservation/
│   │   ├── config/          # Segurança (JWT, CORS) e configurações gerais
│   │   ├── controller/      # Endpoints REST
│   │   ├── domain/          # Entidades JPA (Hotel, Room, Reservation, User)
│   │   ├── dto/             # Objetos de request e response
│   │   ├── enums/           # Tipos de quarto e status de reserva
│   │   ├── exception/       # Handler global de erros
│   │   ├── repository/      # Interfaces JPA com queries customizadas
│   │   └── service/         # Regras de negócio
│   └── resources/
│       ├── db/migration/    # Scripts Flyway (criação de tabelas + seed)
│       └── application.properties
└── test/
```

---

## 📄 Documentos adicionais

- [`incident_analysis.pdf`](./incident_analysis.pdf) — Análise de incidente de race condition (double-booking)
- [`nota_tecnica.pdf`](./nota_tecnica.pdf) — Nota técnica sobre decisões de arquitetura e melhorias futuras
