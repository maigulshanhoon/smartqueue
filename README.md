# SmartQueue — Digital Queue Management System

SmartQueue is a modern, real-time digital queue management platform built with Spring Boot, Thymeleaf, and MySQL. It enables service counters, clinics, and offices to eliminate physical lines, issue virtual tickets, and streamline customer flow.

---

## Prerequisites

- **Java JDK**: Version 21 or higher
- **Maven**: Maven Wrapper (`mvnw` / `mvnw.cmd`) is included in the project repository
- **Database**: MySQL 8.x (for running the application locally or in production)
- **Operating System**: Windows / macOS / Linux

---

## Configuration & Environment Variables

SmartQueue is configured to read its database settings dynamically from environment variables, preventing hardcoded credentials from being committed to source control.

Copy the provided `.env.example` file to `.env`:

```bash
# On Windows PowerShell:
Copy-Item .env.example .env

# On Linux / macOS:
cp .env.example .env
```

Set the following environment variables before starting the application:

| Variable | Description | Example Value |
| :--- | :--- | :--- |
| `DB_URL` | JDBC URL for MySQL database | `jdbc:mysql://localhost:3306/smartqueue?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL database user | `root` |
| `DB_PASSWORD` | MySQL database password | `your_secret_password` |

### Setting Environment Variables in Windows PowerShell

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/smartqueue?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_secret_password"
```

### Setting Environment Variables in Bash / Linux / macOS

```bash
export DB_URL="jdbc:mysql://localhost:3306/smartqueue?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD="your_secret_password"
```

---

## MySQL Database Setup

Ensure MySQL is running and create the `smartqueue` database:

```sql
CREATE DATABASE IF NOT EXISTS smartqueue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## Running Automated Tests

Automated tests are completely independent of a running local MySQL instance. Tests use an in-memory H2 database (`test` profile) configured strictly within test scope.

To run all automated tests:

```powershell
# Windows PowerShell
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

---

## Running the Application Locally

Once your environment variables are configured and MySQL is accessible, launch the application:

```powershell
# Windows PowerShell
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Alternatively, build and run the packaged JAR:

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target/smartqueue-0.0.1-SNAPSHOT.jar
```

---

## Application Endpoints

Once the application is running:

| Endpoint | Method | Description | URL |
| :--- | :--- | :--- | :--- |
| **Home Page** | `GET` | SmartQueue Welcome & Overview UI | [http://localhost:8080/](http://localhost:8080/) |
| **Health API** | `GET` | System Health Check JSON | [http://localhost:8080/api/health](http://localhost:8080/api/health) |

### Sample Health Check Response

`GET http://localhost:8080/api/health`

```json
{
  "status": "UP",
  "application": "SmartQueue"
}
```

---

## Project Structure

```text
smartqueue/
├── .env.example                               # Environment template for DB credentials
├── .gitignore                                 # Git ignore rules including .env and target/
├── pom.xml                                    # Maven configuration and dependencies
├── README.md                                  # Project documentation and setup guide
└── src/
    ├── main/
    │   ├── java/com/smartqueue/
    │   │   ├── SmartqueueApplication.java     # Main Spring Boot application entrypoint
    │   │   └── controller/
    │   │       ├── HomeController.java        # Web MVC controller for GET /
    │   │       └── HealthController.java      # REST controller for GET /api/health
    │   └── resources/
    │       ├── application.properties         # Application configuration (MySQL via env vars)
    │       ├── static/css/
    │       │   └── style.css                  # Responsive styling
    │       └── templates/
    │           └── index.html                 # Thymeleaf welcome template
    └── test/
        ├── java/com/smartqueue/
        │   ├── SmartqueueApplicationTests.java # Context loading test
        │   └── controller/
        │       ├── HomeControllerTest.java    # Automated tests for GET /
        │       └── HealthControllerTest.java  # Automated tests for GET /api/health
        └── resources/
            ├── application.properties         # Test database configuration (H2 in-memory)
            └── application-test.properties    # Profile-specific test database configuration
```
