# SmartQueue — Digital Queue & Token Management System

SmartQueue is a high-performance, real-time digital queue and token management web application built with **Spring Boot 4 (Java 21)**, **Thymeleaf**, **Spring Data JPA**, and **Vanilla CSS**. It is designed for service centers, clinics, banks, and administrative offices to eliminate physical lines, issue virtual tickets, and streamline customer flow.

---

## Table of Contents
1. [Key Features](#key-features)
2. [Application Screens & URLs](#application-screens--urls)
3. [Zero-Setup Demo Fallback (In-Memory H2)](#zero-setup-demo-fallback-in-memory-h2)
4. [Production MySQL Configuration](#production-mysql-configuration)
5. [Demo Admin Credentials & Security](#demo-admin-credentials--security)
6. [REST API Documentation](#rest-api-documentation)
7. [Running Locally](#running-locally)
8. [Automated Testing](#automated-testing)
9. [Render & Docker Deployment](#render--docker-deployment)
10. [Database Architecture & Limitations](#database-architecture--limitations)

---

## Key Features

- **Customer Self-Service Kiosk (`/`)**:
  - Select service category: **General Enquiry**, **Billing**, or **Technical Support**.
  - Enter optional customer name.
  - Automatically issues sequential, unique service tokens (`GEN-001`, `BIL-001`, `TEC-001`).
  - Real-time digital ticket card showing assigned token number, current serving token, people ahead in queue, and calculated estimated wait time.
  - Ticket lookup utility to check status of any existing token.
- **Live Digital Waiting Room Display (`/display`)**:
  - High-contrast, monitor/TV-friendly public display board.
  - Shows currently serving token and counter name prominently.
  - Displays upcoming waiting tokens with service types.
  - Automatic lightweight AJAX polling every 3 seconds for real-time live synchronization.
- **Admin & Counter Operator Dashboard (`/admin`)**:
  - Session-based authentication protecting management operations.
  - Select active counter (e.g. Counter 1, Counter 2, Counter 3).
  - Single-click actions to **Call Next Waiting Customer**, **Complete Service**, or **Skip Token**.
  - Real-time queue statistics: Total Waiting, Currently Serving, Completed Today, Skipped, and Total Created Today.
  - Detailed queue inspection table and recent token history logs.
- **Dual Database Architecture**:
  - **Zero-Setup Demo Fallback**: Automatically boots with an in-memory H2 database if no MySQL environment variables are provided. Perfect for immediate testing and Render live demos.
  - **Production MySQL Mode**: Automatically connects to MySQL 8.x when `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are provided.
- **Render Dynamic Port Compatibility**:
  - Reads Render's dynamic `$PORT` environment variable, defaulting to `8080` locally (`server.port=${PORT:8080}`).

---

## Application Screens & URLs

| Screen | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| **Customer Kiosk** | [`/`](http://localhost:8080/) | Self-service ticket generation & queue status tracking | Public |
| **Live Display Board** | [`/display`](http://localhost:8080/display) | Fullscreen TV board for waiting halls with 3s live polling | Public |
| **Admin Login** | [`/admin/login`](http://localhost:8080/admin/login) | Desk operator authentication portal | Public |
| **Admin Dashboard** | [`/admin`](http://localhost:8080/admin) | Queue calling, token completion, skipping, and stats | Protected (Session) |
| **Health API** | [`/api/health`](http://localhost:8080/api/health) | System health monitoring endpoint | Public |
| **Queue REST API** | [`/api/queue/status`](http://localhost:8080/api/queue/status) | Live queue stats and token lists in JSON format | Public |

---

## Demo Admin Credentials & Security

Management actions (calling next, completing, skipping) require an authenticated admin session.

### Default Demo Credentials:
- **Username**: `admin`
- **Password**: `admin123`

### Customizing Admin Credentials via Environment Variables:
Credentials can be configured via environment variables without modifying code:
```bash
ADMIN_USERNAME=your_custom_admin
ADMIN_PASSWORD=your_secure_password
```

---

## Zero-Setup Demo Fallback (In-Memory H2)

When no `DB_URL` environment variable is provided, SmartQueue automatically launches using an in-memory H2 database:
```properties
spring.datasource.url=jdbc:h2:mem:smartqueuedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
```
- No MySQL server or database installation is required for testing or demonstration.
- Enables zero-config deployment to Render Free Tier immediately.

---

## Production MySQL Configuration

To use persistent MySQL storage, define the following environment variables in `.env` or in your hosting environment:

| Variable | Description | Example Value |
| :--- | :--- | :--- |
| `DB_URL` | JDBC URL for MySQL database | `jdbc:mysql://localhost:3306/smartqueue?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL database username | `root` |
| `DB_PASSWORD` | MySQL database password | `your_secret_password` |
| `PORT` | HTTP port (Render passes this automatically) | `8080` |

### Setting Environment Variables:
**PowerShell:**
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/smartqueue?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
```

**Bash:**
```bash
export DB_URL="jdbc:mysql://localhost:3306/smartqueue?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
```

---

## REST API Documentation

SmartQueue exposes REST endpoints for headless integrations and digital kiosks:

### 1. Queue Status & Statistics
`GET /api/queue/status`
```json
{
  "stats": {
    "totalWaiting": 3,
    "totalServing": 1,
    "totalCompleted": 12,
    "totalSkipped": 1,
    "totalToday": 17
  },
  "currentServing": {
    "id": 15,
    "tokenNumber": "GEN-005",
    "serviceType": "GENERAL_ENQUIRY",
    "customerName": "Alice",
    "status": "SERVING",
    "counterName": "Counter 1"
  },
  "waiting": [
    {
      "id": 16,
      "tokenNumber": "BIL-003",
      "serviceType": "BILLING",
      "customerName": "Bob",
      "status": "WAITING"
    }
  ]
}
```

### 2. Issue a New Token
`POST /api/queue/tokens`
```json
// Request Body
{
  "serviceType": "BILLING",
  "customerName": "Jane Doe"
}

// Response (201 Created)
{
  "status": "SUCCESS",
  "token": {
    "id": 18,
    "tokenNumber": "BIL-004",
    "serviceType": "BILLING",
    "customerName": "Jane Doe",
    "status": "WAITING"
  },
  "peopleAhead": 2,
  "estimatedWaitMinutes": 10
}
```

### 3. Check Specific Token Status
`GET /api/queue/tokens/{tokenNumber}`
- Example: `GET /api/queue/tokens/BIL-004`

### 4. Call Next Customer
`POST /api/queue/call-next`
```json
// Request Body
{
  "counterName": "Counter 2",
  "serviceType": null
}

// Response
{
  "called": true,
  "message": "Token BIL-004 called to Counter 2",
  "token": { ... }
}
```

### 5. Complete / Skip Token
- `POST /api/queue/tokens/{id}/complete`
- `POST /api/queue/tokens/{id}/skip`

---

## Running Locally

### Step 1: Clone and Navigate
```bash
cd smartqueue
```

### Step 2: Run with Maven Wrapper
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

### Step 3: Run Packaged JAR
```powershell
.\mvnw.cmd package -DskipTests
java -jar target/smartqueue-0.0.1-SNAPSHOT.jar
```

Access the application in your browser at `http://localhost:8080`.

---

## Automated Testing

SmartQueue includes comprehensive automated unit, service, and controller integration tests covering:
- Sequential token issuance and formatting (`GEN-001`, `BIL-001`, `TEC-001`)
- Accurate calculation of people ahead and estimated waiting time
- Queue calling, completion, and skip state transitions
- Empty queue handling and invalid ID error handling
- Admin authentication and unauthorized access protection
- REST API token creation, status, and desk operations

### Execute Test Suite:
```powershell
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

*Results: 23 passing tests, 0 failures, 0 errors.*

---

## Render & Docker Deployment

A production-ready multi-stage `Dockerfile` is provided in the repository root.

### Build and Run Docker Container Locally:
```bash
docker build -t smartqueue:latest .
docker run -p 8080:8080 -e PORT=8080 smartqueue:latest
```

### Deploy to Render:
1. Connect your GitHub repository to [Render](https://render.com/).
2. Select **Web Service** and choose **Docker** runtime.
3. Render automatically assigns a dynamic port via the `$PORT` environment variable, which SmartQueue automatically binds to.
4. (Optional) In Render's **Environment** tab, set:
   - `ADMIN_USERNAME`: Your custom admin username
   - `ADMIN_PASSWORD`: Your custom admin password
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`: If connecting an external MySQL database.
5. If no database variables are configured, SmartQueue automatically runs in in-memory H2 demo mode with zero configuration needed.

---

## Database Architecture & Limitations

### In-Memory H2 Mode (Demo Fallback)
- **Advantages**: Fast, zero setup, self-contained, no external database dependencies.
- **Limitations**:
  - Data is stored in memory (`jdbc:h2:mem:smartqueuedb`). All tokens and queue history will reset whenever the application stops, restarts, or is redeployed.
  - Multi-instance deployments cannot share the queue across horizontally scaled replicas without a shared database.

### Persistent MySQL Mode (Production)
- **Advantages**: Full persistence across restarts, historical analytics, scalable across multiple application instances.
- **Recommended Setup**: Provide MySQL 8.x database connection variables in `.env` or container environment settings.
