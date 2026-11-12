# cara pindah path lalu run java

- export JAVA_HOME=/home/fadil/tools/jdk-21.0.12.1+1
- export PATH="$JAVA_HOME/bin:$PATH"
- ./mvnw spring-boot:run

## Arsitektur & Struktur Folder

Aplikasi menerapkan pola arsitektur _Layered Architecture_ (Controller – Service – Repository – Domain) dengan pemisahan tanggung jawab yang tegas:

```
allo-backend-test-main/
├── .env.example                     # Template konfigurasi environment variables
├── docker-compose.yml               # Konfigurasi container PostgreSQL & backend
├── Dockerfile                       # Multi-stage Dockerfile untuk build dan runtime
├── Dockerfile.template              # Template Dockerfile dari requirements
├── pom.xml                          # Konfigurasi Maven dependencies & plugins
├── rest-api/
│   └── split-bill.rest              # HTTP client request examples
├── src/
│   ├── main/
│   │   ├── java/com/allobank/splitbill/
│   │   │   ├── SplitBillApplication.java      # Application entrypoint
│   │   │   ├── controller/
│   │   │   │   └── GroupController.java       # REST endpoints untuk Group, Expense, & Settlement
│   │   │   ├── domain/                        # JPA Entities (Group, Participant, Expense)
│   │   │   │   ├── GroupEntity.java
│   │   │   │   ├── ParticipantEntity.java
│   │   │   │   └── ExpenseEntity.java
│   │   │   ├── dto/                           # Request & Response Records
│   │   │   │   ├── CreateGroupRequest.java
│   │   │   │   ├── GroupResponse.java
│   │   │   │   ├── ParticipantRequest.java
│   │   │   │   ├── ParticipantResponse.java
│   │   │   │   ├── AddExpenseRequest.java
│   │   │   │   ├── ExpenseResponse.java
│   │   │   │   ├── SettlementSummaryResponse.java
│   │   │   │   ├── SettlementTransactionResponse.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── exception/                     # Centralized Error Handling
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── repository/                    # Spring Data JPA Repositories
│   │   │   │   ├── GroupRepository.java
│   │   │   │   ├── ParticipantRepository.java
│   │   │   │   └── ExpenseRepository.java
│   │   │   └── service/                       # Business Logic Layer
│   │   │       ├── GroupService.java
│   │   │       ├── ExpenseService.java
│   │   │       ├── ServiceChargeCalculator.java # Perhitungan service charge dinamis
│   │   │       └── SettlementCalculator.java    # Algoritma greedy settlement & sisa desimal
│   │   └── resources/
│   │       ├── application.yml                # Konfigurasi runtime default
│   │       ├── application-test.yml           # Konfigurasi unit/integration test (H2)
│   │       └── db/migration/
│   │           └── V1__init_schema.sql        # Migrasi Flyway untuk tabel PostgreSQL
│   └── test/
│       └── java/com/allobank/splitbill/
│           ├── GroupControllerIntegrationTest.java    # Full workflow & validation test
│           └── service/
│               ├── ServiceChargeCalculatorTest.java   # Unit test service charge
│               └── SettlementCalculatorTest.java      # Unit test 8 skenario settlement
```

# Split Bill REST API

A REST API application for managing group expenses and calculating debt settlements fairly with optimized transactions.

Built for the **Allo Bank Backend Developer Take-Home Test**.

## Tech Stack

- Java 21
- Spring Boot 3.3.3
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Maven
- Docker & Docker Compose
- JUnit 5 & MockMvc

## Features

- Create expense groups with multiple participants.
- Retrieve group details and participants.
- Add expenses and assign participants.
- View group expense history.
- Calculate debt settlements with optimized transactions.
- Dynamic service charge based on GitHub username.
- Input validation and centralized error handling.

## Getting Started

### Prerequisites

- Java 21 (if running locally)
- Docker & Docker Compose (recommended)

### Run with Docker

1. Clone the repository:

   ```bash
   git clone https://github.com/ahmad-user/allo-backend-test.git
   cd allo-backend-test
   ```

2. Create environment configuration:

   ```bash
   cp .env.example .env
   ```

3. Start the application:

   ```bash
   docker compose up --build -d
   ```

4. Access the API:

   ```text
   http://localhost:4110
   ```

### Run Locally

Make sure Java 21 and PostgreSQL 16 are installed and PostgreSQL is running.

```bash
git clone https://github.com/ahmad-user/allo-backend-test.git
cd allo-backend-test

./mvnw spring-boot:run
```

The application will run at `http://localhost:4110`.

The default database configuration is available in `application.yml`. Make sure the database `splitbill` exists and the connection credentials match your PostgreSQL setup.

## API Endpoints

Base URL: `http://localhost:4110`

| Method | Endpoint                           | Description          |
| ------ | ---------------------------------- | -------------------- |
| POST   | `/api/groups`                      | Create a group       |
| GET    | `/api/groups/{groupId}`            | Get group details    |
| POST   | `/api/groups/{groupId}/expenses`   | Add an expense       |
| GET    | `/api/groups/{groupId}/expenses`   | Get group expenses   |
| GET    | `/api/groups/{groupId}/settlement` | Calculate settlement |

## Testing

Run all unit and integration tests:

```bash
./mvnw clean test
```

## API Testing

API requests are available in:

```text
rest-api/api.http
```

Use the **REST Client** extension in Visual Studio Code to execute and test the endpoints.

## Stop Application

```bash
docker compose down
```

## Author

**Ahmad Fadilah**

GitHub: [@ahmad-user](https://github.com/ahmad-user)
