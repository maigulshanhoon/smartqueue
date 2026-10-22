# WaitWise — Digital Queue & Token Management System

> **“Less Waiting. More Doing.”**

**WaitWise** is a modern, high-performance, real-time digital queue and token management web application built with **Spring Boot 4 (Java 21)**, **Thymeleaf**, **Spring Data JPA**, and **Vanilla CSS**.

It is designed for service counters, clinics, banking halls, customer support centers, and administrative offices to eliminate physical lines, issue virtual tickets, and streamline customer flow.

---

## Table of Contents
1. [Core Features](#core-features)
2. [Application Screens & URLs](#application-screens--urls)
3. [Admin Panel Capabilities](#admin-panel-capabilities)
4. [Demo Admin Credentials & Security](#demo-admin-credentials--security)
5. [Zero-Setup Demo Fallback (In-Memory H2)](#zero-setup-demo-fallback-in-memory-h2)
6. [Production MySQL Configuration](#production-mysql-configuration)
7. [REST API Documentation](#rest-api-documentation)
8. [Automated Testing](#automated-testing)
9. [Running Locally](#running-locally)
10. [Render & Docker Deployment](#render--docker-deployment)
11. [Limitations of In-Memory Mode](#limitations-of-in-memory-mode)

---

## Core Features

- **Branding & Visual Excellence**:
  - Consistent modern indigo/purple design system with dark navy and clean white cards.
  - Responsive centered layout optimized for mobile phones, tablets, desktop workstations, and waiting-room TV displays.
  - Tagline: *“Less Waiting. More Doing.”*
- **Customer Self-Service Kiosk (`/`)**:
  - 4 aligned summary cards: Currently Serving, Waiting in Line, Served Today, and Total Tokens.
  - Service selection cards with clear separation between service name, description, and average wait time.
  - Full-width, comfortable customer name input (handles long names without awkward wrapping).
  - Instant unique, sequential token generation (`GEN-001`, `BIL-001`, `TEC-001`).
  - Digital Ticket Card with real-time status, people ahead, estimated wait time, and counter call instructions.
  - Integrated ticket search and tracking by token number.
- **Live Waiting Room TV Display (`/display`)**:
  - High-contrast, dark-mode board designed for lobby monitors and TVs.
  - Ultra-large, high-visibility "Now Serving" token number visible from across the room.
  - Active counters section and upcoming queue entries.
  - Polished empty states when counters are open or queue is clear.
  - Real-time 3-second live AJAX polling without full-page reloads, with visual flash pulse on new calls.
- **Admin & Counter Operator Operations Console (`/admin`)**:
  - Session-protected portal for counter operators and administrators.
  - Real-time KPI summary: Waiting, Serving, Served Today, Cancelled/Skipped, and Total Today.
  - Service volume breakdown chips (General Enquiry, Billing, Tech Support).
  - **Desk Dispatcher**: Select active counter (Counter 1-4, Help Desk) and filter by service to call the next customer.
  - **Active Desks Section**: Directly **Mark Served**, **Skip**, or **Cancel** tickets currently at counters.
  - **Waiting Queue Section**: View queued customers in order of arrival with options to summon or cancel.
  - **Complete Queue Registry**: Full searchable and filterable table (filter by status and service).
  - **Recent Activity Log**: Audit trail of the last 15 customer transactions.
  - Safe server-side state machine: prevents invalid transitions (e.g., serving a cancelled token).

---

## Application Screens & URLs

| Screen | Route | Description | Access |
| :--- | :--- | :--- | :--- |
| **Customer Kiosk** | [`/`](http://localhost:8080/) | Self-service ticket issuance & status tracking | Public |
| **Live TV Display** | [`/display`](http://localhost:8080/display) | Fullscreen waiting room display with 3s live polling | Public |
| **Admin Login** | [`/admin/login`](http://localhost:8080/admin/login) | Operator sign-in portal | Public |
| **Admin Dashboard** | [`/admin`](http://localhost:8080/admin) | Queue dispatching, serving, cancellation, and registry | Protected (Session) |
| **Health API** | [`/api/health`](http://localhost:8080/api/health) | Health status JSON | Public |
| **Queue REST API** | [`/api/queue/status`](http://localhost:8080/api/queue/status) | Live queue metrics and token list in JSON | Public |

---

## Admin Panel Capabilities

The WaitWise Admin Panel provides a full set of desk operations:

1. **Call Next Customer** (`POST /admin/call-next`):
   - Summons the next waiting ticket in FIFO order for the chosen counter desk and service filter.
   - Updates token status to `SERVING`, sets `servedAt` timestamp, and assigns the counter name.
2. **Mark as Served** (`POST /admin/tokens/{id}/complete`):
   - Transitions an active serving token to `COMPLETED` ("Served").
   - Records the completion timestamp.
3. **Cancel Token** (`POST /admin/tokens/{id}/cancel`):
   - Transitions a waiting or serving token to `CANCELLED`.
   - Safely removes the customer from the waiting queue and records the event.
4. **Skip / No-Show** (`POST /admin/tokens/{id}/skip`):
   - Marks an absent customer as `SKIPPED`.
5. **Direct Counter Call** (`POST /admin/tokens/{id}/call`):
   - Allows an operator to summon a specific waiting token directly.
6. **Registry Search & Filter**:
   - Filter by status: *Waiting, Now Serving, Served, Cancelled, Skipped*.
   - Filter by service: *General Enquiry, Billing, Technical Support*.
   - Instant search by token number or customer name.

---

## Demo Admin Credentials & Security

Management actions (calling, completing, skipping, cancelling) require an authenticated session.

### Default Demo Credentials:
- **Username**: `admin`
- **Password**: `admin123`

### Customizing Credentials via Environment Variables:
Credentials can be customized without altering any source code:
```bash
ADMIN_USERNAME=your_custom_admin
ADMIN_PASSWORD=your_secure_password
```

---

## Zero-Setup Demo Fallback (In-Memory H2)

When no `DB_URL` environment variable is supplied, WaitWise automatically starts using an in-memory H2 database:
```properties
spring.datasource.url=jdbc:h2:mem:smartqueuedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
```
- No database installation or cloud database setup is needed to test or demo the app.
- Deploys instantly to Render Free Tier out of the box.

---

## Production MySQL Configuration

To use persistent MySQL storage, supply standard environment variables:

| Variable | Description | Example Value |
| :--- | :--- | :--- |
| `DB_URL` | JDBC URL for MySQL database | `jdbc:mysql://localhost:3306/smartqueue?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL database user | `root` |
| `DB_PASSWORD` | MySQL database password | `your_secret_password` |
| `PORT` | Dynamic HTTP port (Render supplies this automatically) | `8080` |

---

## REST API Documentation

### 1. Queue Status & Metrics
`GET /api/queue/status`
```json
{
  "stats": {
    "totalWaiting": 2,
    "totalServing": 1,
    "totalCompleted": 8,
    "totalSkipped": 0,
    "totalCancelled": 1,
    "totalToday": 12,
    "byService": {
      "General Enquiry": 5,
      "Billing": 4,
      "Technical Support": 3
    }
  },
  "nowServing": [ ... ],
  "waiting": [ ... ]
}
```

### 2. Issue Token
`POST /api/queue/tokens`
```json
// Request Body
{
  "serviceType": "BILLING",
  "customerName": "Alice Walker"
}

// Response (201 Created)
{
  "status": "SUCCESS",
  "token": {
    "id": 14,
    "tokenNumber": "BIL-004",
    "serviceType": "BILLING",
    "customerName": "Alice Walker",
    "status": "WAITING"
  },
  "peopleAhead": 1,
  "estimatedWaitMinutes": 10
}
```

### 3. Check Specific Ticket
`GET /api/queue/tokens/{tokenNumber}`

### 4. Operational Actions
- Call Next: `POST /api/queue/call-next` (Body: `{"counterName":"Counter 1"}`)
- Complete / Served: `POST /api/queue/tokens/{id}/complete`
- Cancel: `POST /api/queue/tokens/{id}/cancel`
- Skip: `POST /api/queue/tokens/{id}/skip`

---

## Automated Testing

The automated test suite runs independently of any external database using an in-memory test profile.

```powershell
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

**Verification Results:**
- **27 tests executed**, **0 failures**, **0 errors**, **0 skipped**.
- Full coverage of token generation, FIFO ordering, wait time calculation, next calling, mark served, cancellation, skipped tokens, invalid transition guards, admin authentication, unauthorized redirects, live display, and REST APIs.

---

## Running Locally

```powershell
# Run using Maven
.\mvnw.cmd spring-boot:run

# Or run the prepackaged JAR
java -jar target/smartqueue-0.0.1-SNAPSHOT.jar
```
Visit [http://localhost:8080/](http://localhost:8080/) in your browser.

---

## Render & Docker Deployment

WaitWise includes a production-grade multi-stage `Dockerfile` configured to bind to Render’s dynamic `$PORT` environment variable (`server.port=${PORT:8080}`).

```bash
docker build -t waitwise:latest .
docker run -p 8080:8080 -e PORT=8080 waitwise:latest
```

---

## Limitations of In-Memory Mode

- In demo mode (when `DB_URL` is omitted), data is stored in memory (`jdbc:h2:mem:smartqueuedb`).
- **Limitation**: Any active tokens or queue statistics reset whenever the application stops, restarts, or is redeployed on Render.
- For permanent persistence, connect a managed MySQL database using the `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` variables.
