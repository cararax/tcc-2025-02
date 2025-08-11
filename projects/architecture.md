# High-Level Architecture

```mermaid
graph TD
    subgraph "Client"
        A[Web/Mobile App]
    end

    subgraph "API Gateway"
        B[Spring Cloud Gateway]
    end

    subgraph "Axon-based Microservices"
        C[Reservation Service]
        D[Payment Service]
        E[Notification Service]
    end

    subgraph "Messaging & Event Store"
        F[Axon Server / RabbitMQ]
    end

    A --> B
    B --> C
    B --> D
    B --> E

    C <--> F
    D <--> F
    E <--> F