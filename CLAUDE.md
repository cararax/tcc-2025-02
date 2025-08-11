# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a TCC (Thesis/Final Project) repository demonstrating microservices architecture with the Saga pattern implementation using orchestration. The project simulates a ticket reservation system called "ReservaExpress" with distributed transaction management.

## Architecture

The system consists of four main microservices:
- **Saga Orchestrator** (`saga-orchestrator/`) - Central service managing transaction lifecycle
- **Reservation Service** (`reservation-service/`) - Handles reservation creation/cancellation
- **Payment Service** (`payment-service/`) - Processes payments and refunds
- **Notification Service** (`notification-service/`) - Sends confirmation notifications

Each service follows the "database per service" pattern with individual PostgreSQL instances and communicates asynchronously via RabbitMQ.

## Development Commands

### Building the Project
```bash
# Build individual services (run in each service directory)
mvn clean install

# Build all services from projects root
cd projects/saga-orchestrator && mvn clean install
cd ../reservation-service && mvn clean install  
cd ../payment-service && mvn clean install
cd ../notification-service && mvn clean install
```

### Running the System
```bash
# Note: Docker Compose file exists but is empty - manual PostgreSQL setup required
# Each service expects its own PostgreSQL database:
# - saga-orchestrator: localhost:5430/saga_db (saga_user/saga_pass)
# - reservation-service: localhost:5431/reservation_db (reservation_user/reservation_pass)
# - payment-service: localhost:5432/payment_db (payment_user/payment_pass) 
# - notification-service: localhost:5433/notification_db (notification_user/notification_pass)

# Run each microservice (in separate terminals)
cd projects/saga-orchestrator && mvn spring-boot:run
cd projects/reservation-service && mvn spring-boot:run  
cd projects/payment-service && mvn spring-boot:run
cd projects/notification-service && mvn spring-boot:run
```

### Testing
```bash
# Run unit tests for individual services
mvn test

# API testing via Swagger UI (each service has its own):
# - Saga Orchestrator: http://localhost:8080/swagger-ui.html
# - Reservation Service: http://localhost:8081/swagger-ui.html  
# - Payment Service: http://localhost:8082/swagger-ui.html
# - Notification Service: http://localhost:8083/swagger-ui.html
```

## Key Service Patterns

### Saga Pattern Implementation
- **Orchestration-based**: Central orchestrator manages transaction flow
- **Async messaging**: RabbitMQ for command/reply communication
- **Compensation logic**: Automatic rollback on failures
- **State persistence**: Each saga state tracked in database

### Message Flow
1. HTTP request → Orchestrator Controller
2. Saga entity created with `STARTED` status
3. Async command sent via RabbitMQ (e.g., `CREATE_RESERVATION`)
4. Service processes and sends reply (`SUCCEEDED`/`FAILED`)
5. Orchestrator continues to next step or initiates compensation

### Service Structure (Each Service)
```
src/main/java/com/carara/{service}/
├── {Service}Application.java     # Spring Boot main class
├── config/
│   └── RabbitMQConfig.java      # Message broker configuration
├── controller/
│   └── {Service}Controller.java  # REST endpoints
├── model/                       # Domain entities and DTOs
├── repository/                  # Data access layer
└── service/
    ├── {Service}Service.java    # Business logic
    └── {Service}Listener.java   # Message consumption
```

## Technology Stack

- **Java 17** with Spring Boot 3.2.3
- **Spring Data JPA** with PostgreSQL databases  
- **Spring AMQP** for RabbitMQ messaging
- **SpringDoc OpenAPI** (Swagger) for API documentation
- **Lombok** for reducing boilerplate code
- **Maven** for build management

## Service Configuration

Each service runs on a different port and connects to its own PostgreSQL database:
- **Saga Orchestrator**: Port 8080, DB on 5430
- **Reservation Service**: Port 8081, DB on 5431  
- **Payment Service**: Port 8082, DB on 5432
- **Notification Service**: Port 8083, DB on 5433

## Important Implementation Notes

- Services use **@RestController** with OpenAPI annotations for comprehensive API documentation
- JPA entities with Hibernate auto-DDL for database schema management
- Custom service URLs configured in application.yml for inter-service communication
- **No Maven wrapper** present in individual services (only in demo project)
- **Docker Compose configuration exists but is empty** - requires manual infrastructure setup
- Services are **fully implemented** with working controllers, services, and data persistence

## Documentation Structure

- `/projects/README.md` - Main project documentation
- `/projects/architecture.md` - High-level architecture overview
- `/resources/` - Academic papers and research materials
- `/text/` - Thesis documentation and diagrams