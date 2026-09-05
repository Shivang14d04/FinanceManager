# Personal Finance Manager API

A robust, production-ready **Spring Boot 3** REST API designed for personal financial management. The system enables users to track income and expenses, manage categories, set savings goals with automated progress tracking, and generate financial reports.

---

## 📋 Table of Contents
* [Features](#-features)
* [Technology Stack](#-technology-stack)
* [Architecture & Design Decisions](#-architecture--design-decisions)
* [Setup Guide](#-setup-guide)
* [Usage Guide & Testing](#-usage-guide--testing)
* [API Endpoint Reference](#-api-endpoint-reference)

---

## 🌟 Features

### 1. User Management & Session Authentication
* **Registration & Login**: Validates email format, strong passwords, full name, and phone number.
* **Session Management**: Implements session-based authentication using secure cookies (`JSESSIONID`).
* **Data Isolation**: Ensures complete multi-tenant data segregation; users can only access their own data.

### 2. Transaction Management
* **Full CRUD Operations**: Create, read, update, and delete financial transactions.
* **Filtering & Sorting**: View transactions sorted newest first, with query filters for date range (`startDate`, `endDate`) and category (`categoryId`).
* **Validations**: Enforces positive decimal amounts and prevents future transaction dates.

### 3. Category Management
* **Default Categories**: Predefined non-deletable categories (*Salary* for INCOME; *Food, Rent, Transportation, Entertainment, Healthcare, Utilities* for EXPENSE).
* **Custom Categories**: Support for user-defined categories.
* **Referential Protection**: Prevents deletion of categories currently referenced by active transactions.

### 4. Savings Goals
* **Goal Setting**: Track savings targets with target amounts and future completion dates.
* **Automated Progress Tracking**: Calculates progress dynamically as `(Total Income - Total Expenses)` incurred since the goal start date.
* **Calculated Metrics**: Provides percentage completion and remaining target amount.

### 5. Reports & Analytics
* **Monthly Reports**: Category-wise breakdown of income, expenses, and net savings for any given month/year.
* **Yearly Reports**: Aggregated annual financial performance summary.

---

## 🛠️ Technology Stack

* **Language**: Java 17
* **Framework**: Spring Boot 3.4 / 4.x
* **Security**: Spring Security (BCrypt Password Hashing, Session Authentication)
* **Database**: PostgreSQL (Production) / H2 (Local Development)
* **Testing**: JUnit 5, Mockito (28 Unit Tests, >80% coverage)
* **Build Tool**: Maven (`mvnw`)
* **Containerization**: Docker (Multi-stage build)

---

## 🏗️ Architecture & Design Decisions

* **Layered Architecture**: Strictly follows `Controller → Service → Repository` separation of concerns.
* **DTO Pattern**: Requests and responses use record DTOs (`RegisterRequest`, `TransactionResponse`, etc.) to keep JPA database entities decoupled from external API contracts.
* **Global Exception Handler**: Implements `@ControllerAdvice` (`GlobalExceptionHandler`) to intercept runtime exceptions (`BadRequestException`, `ConflictException`, `UnauthorizedException`, `ResourceNotFoundException`) and return standard HTTP error responses (400, 401, 403, 404, 409) without leaking 5xx stack traces.
* **Externalized Configuration**: Uses environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`) for environment portability.

---

## ⚙️ Setup Guide

### Prerequisites
* **Java 17+** installed (`java -version`)
* **Git** installed

### 1. Clone the Repository
```bash
git clone git@github.com:Shivang14d04/FinanceManager.git
cd FinanceManager/FinanceManager
```

### 2. Local Environment Setup
By default, the application runs against PostgreSQL or H2 database. Configure your environment variables or local properties in `src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/FinanceManager}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:password}
```

### 3. Build & Run Application
Use the included Maven wrapper to build and start the server:

```bash
# Build executable JAR
./mvnw clean package -DskipTests

# Run Spring Boot Application
./mvnw spring-boot:run
```
The server will start at `http://localhost:8080`.

---

## 🧪 Usage Guide & Testing

### Running Unit Tests (JUnit 5 + Mockito)
The codebase includes 28 unit tests covering all service logic and edge cases:

```bash
./mvnw test
```

### Running Deployed Integration Test Script
To test a live API deployment against all 86 assignment assertions:

```bash
chmod +x financial_manager_tests.sh
./financial_manager_tests.sh https://<your-render-url>.onrender.com/api
```

---

## 📖 API Endpoint Reference

All protected endpoints require an active session cookie obtained via `/api/auth/login`.

### Authentication
* `POST /api/auth/register` - Register a new user account.
* `POST /api/auth/login` - Authenticate user and initiate session.
* `POST /api/auth/logout` - Expire and invalidate active session.

### Categories
* `GET /api/categories` - Fetch all accessible default & custom categories.
* `POST /api/categories` - Create a new custom category.
* `DELETE /api/categories/{name}` - Delete a custom category.

### Transactions
* `POST /api/transactions` - Record a new transaction.
* `GET /api/transactions` - List transactions (supports `startDate`, `endDate`, `categoryId` filters).
* `PUT /api/transactions/{id}` - Update transaction details.
* `DELETE /api/transactions/{id}` - Delete a transaction.

### Savings Goals
* `POST /api/goals` - Establish a new savings goal.
* `GET /api/goals` - List all savings goals with calculated progress.
* `GET /api/goals/{id}` - Retrieve specific savings goal details.
* `PUT /api/goals/{id}` - Update target amount or target date.
* `DELETE /api/goals/{id}` - Remove a savings goal.

### Reports & Analytics
* `GET /api/reports/monthly/{year}/{month}` - Generate monthly category breakdown & net savings.
* `GET /api/reports/yearly/{year}` - Generate annual financial summary.
