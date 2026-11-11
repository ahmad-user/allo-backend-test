# cara pindah path lalu run java

- export JAVA_HOME=/home/fadil/tools/jdk-21.0.12.1+1
- export PATH="$JAVA_HOME/bin:$PATH"
- ./mvnw spring-boot:run

# Split Bill REST API — Allo Bank Backend Challenge

Aplikasi REST API backend berbasis Spring Boot yang dirancang untuk mengelola pembagian pengeluaran bersama (_split bill_) dalam sebuah kelompok (misalnya perjalanan liburan, sewa tempat tinggal, atau makan bersama), serta menghitung skema penyelesaian utang (_settlement_) secara adil dan optimal dengan meminimalkan jumlah transaksi antaranggotanya.

Aplikasi ini dibangun untuk memenuhi kriteria evaluasi teknis Allo Bank Backend Developer Take-Home Test, dengan fokus pada akurasi finansial, arsitektur modular, validasi data menyeluruh, serta kesiapan _production_.

---

## Tech Stack

- **Java**: Java 21 (Eclipse Temurin 21)
- **Framework**: Spring Boot 3.3.3 (Spring Web, Spring Data JPA, Spring Validation)
- **Build Tool**: Apache Maven (menggunakan Maven Wrapper `./mvnw`)
- **Database**: PostgreSQL 16 (H2 In-Memory untuk automated test profile)
- **Database Migration**: Flyway
- **Containerization**: Docker & Docker Compose (Multi-stage build)
- **Testing**: JUnit 5, MockMvc, Hamcrest

---

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

---

## Environment Variables

Berikut variabel lingkungan yang didukung aplikasi:

| Variabel                     | Deskripsi                                      | Default Value                                |
| :--------------------------- | :--------------------------------------------- | :------------------------------------------- |
| `PORT`                       | Port HTTP tempat server mendengarkan           | `4110`                                       |
| `SPRING_DATASOURCE_URL`      | JDBC URL untuk database PostgreSQL             | `jdbc:postgresql://localhost:5432/splitbill` |
| `SPRING_DATASOURCE_USERNAME` | Username database PostgreSQL                   | `postgres`                                   |
| `SPRING_DATASOURCE_PASSWORD` | Password database PostgreSQL                   | `postgres`                                   |
| `POSTGRES_DB`                | Nama database PostgreSQL (Docker)              | `splitbill`                                  |
| `POSTGRES_USER`              | User PostgreSQL (Docker)                       | `postgres`                                   |
| `POSTGRES_PASSWORD`          | Password PostgreSQL (Docker)                   | `postgres`                                   |
| `POSTGRES_PORT`              | Port mapping PostgreSQL ke host                | `5432`                                       |
| `GITHUB_USERNAME`            | Username GitHub untuk kalkulasi service charge | `ahmad-user`                                 |

---

## Cara Menjalankan Aplikasi

### 1. Menjalankan Menggunakan Maven Wrapper (Lokal)

Pastikan Java 21 terpasang dan database PostgreSQL sedang berjalan secara lokal.

1. Salin file environment:
   ```bash
   cp .env.example .env
   ```
2. Jalankan migrasi Flyway & aplikasi Spring Boot:

   ```bash
   ./mvnw spring-boot:run
   ```

   Aplikasi akan aktif dan dapat diakses pada `http://localhost:4110`.

3. Menjalankan seluruh Unit Test dan Integration Test:
   ```bash
   ./mvnw clean test
   ```

### 2. Menjalankan Menggunakan Docker Compose

Docker Compose akan menjalankan kontainer PostgreSQL 16 Alpine dan backend Spring Boot multi-stage secara otomatis dengan _healthcheck_:

```bash
docker compose up --build -d
```

Untuk memantau log aplikasi:

```bash
docker compose logs -f backend
```

Untuk menghentikan kontainer:

```bash
docker compose down
```

---

## Endpoint API

Base URL: `http://localhost:4110`

| HTTP Method | Endpoint                           | Deskripsi                                                                                     |
| :---------- | :--------------------------------- | :-------------------------------------------------------------------------------------------- |
| `POST`      | `/api/groups`                      | Membuat grup penagihan baru beserta daftar anggota awal                                       |
| `GET`       | `/api/groups/{groupId}`            | Mengambil informasi detail grup beserta daftar anggotanya                                     |
| `POST`      | `/api/groups/{groupId}/expenses`   | Menambahkan pengeluaran baru ke dalam grup                                                    |
| `GET`       | `/api/groups/{groupId}/expenses`   | Mengambil seluruh riwayat pengeluaran dalam sebuah grup                                       |
| `GET`       | `/api/groups/{groupId}/settlement` | Menghitung dan mengambil ringkasan penyelesaian utang (_settlement_) beserta _service charge_ |

---

## Contoh `curl` untuk Setiap Endpoint

### 1. Create Bill Group (`POST /api/groups`)

Membuat grup dengan nama "Trip Bandung" beranggotakan Alice, Bob, dan Charlie:

```bash
curl -X POST http://localhost:4110/api/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Trip Bandung",
    "participants": [
      {"name": "Alice"},
      {"name": "Bob"},
      {"name": "Charlie"}
    ]
  }'
```

### 2. Get Group Details (`GET /api/groups/{groupId}`)

Mengambil informasi grup dengan ID 1:

```bash
curl -X GET http://localhost:4110/api/groups/1
```

### 3. Add Expense (`POST /api/groups/{groupId}/expenses`)

Menambahkan pengeluaran makan malam sebesar Rp300.000 yang dibayarkan oleh Alice (ID: 1) untuk Alice, Bob, dan Charlie:

```bash
curl -X POST http://localhost:4110/api/groups/1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Dinner",
    "amount": 300000.00,
    "paidByParticipantId": 1,
    "participantIds": [1, 2, 3]
  }'
```

Menambahkan pengeluaran taksi sebesar Rp90.000 yang dibayarkan oleh Bob (ID: 2) untuk Bob dan Charlie:

```bash
curl -X POST http://localhost:4110/api/groups/1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Taxi",
    "amount": 90000.00,
    "paidByParticipantId": 2,
    "participantIds": [2, 3]
  }'
```

### 4. Get Expenses List (`GET /api/groups/{groupId}/expenses`)

Mengambil seluruh daftar pengeluaran pada grup ID 1:

```bash
curl -X GET http://localhost:4110/api/groups/1/expenses
```

### 5. Get Settlement Summary (`GET /api/groups/{groupId}/settlement`)

Mengambil ringkasan kalkulasi penyelesaian utang piutang dan biaya layanan:

```bash
curl -X GET http://localhost:4110/api/groups/1/settlement
```

---

## Contoh Response JSON Settlement

Berikut adalah contoh payload respons JSON saat memanggil endpoint `GET /api/groups/1/settlement`:

```json
{
  "group_id": 1,
  "group_name": "Trip Bandung",
  "total_expenses": 390000.0,
  "service_charge_pct": 9,
  "service_charge_amount": 35100.0,
  "settlements": [
    {
      "from_participant_id": 3,
      "from_participant_name": "Charlie",
      "to_participant_id": 1,
      "to_participant_name": "Alice",
      "amount": 145000.0
    },
    {
      "from_participant_id": 2,
      "from_participant_name": "Bob",
      "to_participant_id": 1,
      "to_participant_name": "Alice",
      "amount": 55000.0
    }
  ]
}
```

---

## Personalization & Perhitungan Service Charge

- **GitHub Username**: `ahmad-user`
- **Aturan Perhitungan**:
  1. Ubah username ke huruf kecil (_lowercase_): `"ahmad-user"`.
  2. Jumlahkan nilai Unicode / ASCII setiap karakternya:
     - `'a'` = 97
     - `'h'` = 104
     - `'m'` = 109
     - `'a'` = 97
     - `'d'` = 100
     - `'-'` = 45
     - `'u'` = 117
     - `'s'` = 115
     - `'e'` = 101
     - `'r'` = 114
     - **Total Unicode Sum** = `97 + 104 + 109 + 97 + 100 + 45 + 117 + 115 + 101 + 114` = **999**
  3. Modulo 10:
     - `service_charge_pct` = `999 % 10` = **9%**
  4. Perhitungan Nominal Service Charge:
     - Pada skenario total pengeluaran **Rp390.000,00**:
     - `service_charge_amount = Rp390.000,00 * 9%` = **Rp35.100,00**
