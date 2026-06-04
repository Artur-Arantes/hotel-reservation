# Hotel Reservation API

![CI](https://github.com/Artur-Arantes/hotel-reservation/actions/workflows/ci.yml/badge.svg)
![Coverage](.github/badges/jacoco.svg)

REST API for hotel reservation management built with Spring Boot 4 and Java 21.

## Technologies

- **Java 21** + **Spring Boot 4**
- **Spring Security** with JWT authentication and refresh tokens
- **Spring Data JPA** + **MySQL**
- **Testcontainers** for integration tests
- **JaCoCo** with 85% line coverage threshold
- **SpringDoc OpenAPI** (Swagger UI)

## Features

- User registration and login with JWT access token + refresh token
- Hotel management (create, list, get by ID)
- Room management (create, delete, availability check)
- Reservation management (create, list, cancel)
- Pagination on list endpoints
- Global exception handling with proper HTTP status codes
- Optimistic locking via `@Version` on all entities

## Getting Started

### Prerequisites

- Java 21
- Maven
- Docker (for running MySQL via Testcontainers on tests)

### Running locally

1. Start a MySQL instance and set the environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/hotel
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root
export SECRET_KEY=your-secret-key
```

2. Run the application:

```bash
./mvnw spring-boot:run
```

3. Access the Swagger UI at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Running tests

```bash
./mvnw verify
```

> Tests require Docker running (Testcontainers spins up a MySQL container automatically).

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate and get tokens |
| POST | `/api/auth/refresh` | Refresh access token |

### Hotels
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/hotels` | List all hotels (paginated) |
| GET | `/api/hotels/{id}` | Get hotel by ID |
| POST | `/api/hotels` | Create a hotel |

### Rooms
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rooms/{id}` | Get room by ID |
| POST | `/api/rooms` | Create a room |
| DELETE | `/api/rooms/{id}` | Delete a room |
| POST | `/api/rooms/availability` | Check available rooms |

### Reservations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reservations` | List all reservations (paginated) |
| GET | `/api/reservations/{id}` | Get reservation by ID |
| POST | `/api/reservations` | Create a reservation |
| PATCH | `/api/reservations/{id}/cancel` | Cancel a reservation |
