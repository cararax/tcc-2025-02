# 📋 Documentação dos Microsserviços - Padrão Saga

Este projeto demonstra a implementação do padrão Saga com orquestração para gerenciar transações distribuídas em uma arquitetura de microsserviços.

## 🏗️ Arquitetura do Sistema

### Visão Geral
O sistema simula um processo de reserva de passagens (`ReservaExpress`) composto por:

- **Saga Orchestrator** (Porta 8080) - Orquestra o fluxo da transação
- **Reservation Service** (Porta 8081) - Gerencia reservas
- **Payment Service** (Porta 8082) - Processa pagamentos
- **Notification Service** (Porta 8083) - Envia notificações

## 🚀 Como Executar o Sistema

### 1. Pré-requisitos
- Docker e Docker Compose
- Java 17+
- IntelliJ IDEA (com Maven integrado)

### 2. Inicializar Infraestrutura
```bash
cd /home/carara/Documents/tcc-2025-02/projects
docker-compose up -d
```

### 3. Executar os Serviços (IntelliJ)
Execute na seguinte ordem:
1. `saga-orchestrator/src/main/java/com/carara/saga/SagaApplication.java`
2. `reservation-service/src/main/java/com/carara/reservation/ReservationServiceApplication.java`
3. `payment-service/src/main/java/com/carara/payment/PaymentServiceApplication.java`
4. `notification-service/src/main/java/com/carara/notification/NotificationServiceApplication.java`

### 4. Verificar Status
- **Saga Orchestrator**: http://localhost:8080/swagger-ui.html
- **Reservation Service**: http://localhost:8081/swagger-ui.html
- **Payment Service**: http://localhost:8082/swagger-ui.html
- **Notification Service**: http://localhost:8083/swagger-ui.html

## 🗄️ Configuração das Bases de Dados

### Conexões PostgreSQL
| Serviço | Host | Porta | Database | Usuário | Senha |
|---------|------|--------|----------|---------|-------|
| Saga Orchestrator | localhost | 5430 | saga_db | saga_user | saga_pass |
| Reservation Service | localhost | 5431 | reservation_db | reservation_user | reservation_pass |
| Payment Service | localhost | 5432 | payment_db | payment_user | payment_pass |
| Notification Service | localhost | 5433 | notification_db | notification_user | notification_pass |

### Acesso às Bases de Dados
```bash
# Saga Orchestrator DB
docker exec -it saga-db psql -U saga_user -d saga_db

# Reservation Service DB
docker exec -it reservation-db psql -U reservation_user -d reservation_db

# Payment Service DB
docker exec -it payment-db psql -U payment_user -d payment_db

# Notification Service DB
docker exec -it notification-db psql -U notification_user -d notification_db
```

## 📊 Estrutura de Dados Persistidas

### 1. Saga Orchestrator (saga_db)
**Tabela: transaction_context** (Futura implementação)
- Armazena contexto das transações saga
- Status de cada step
- Informações de compensação

### 2. Reservation Service (reservation_db)
**Tabela: reservation**
```sql
CREATE TABLE reservation (
    reservation_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),
    journey_date VARCHAR(255),
    status VARCHAR(50),
    seat_number VARCHAR(10),
    amount DECIMAL(10,2)
);
```

### 3. Payment Service (payment_db)
**Tabela: payment**
```sql
CREATE TABLE payment (
    payment_id VARCHAR(255) PRIMARY KEY,
    reservation_id VARCHAR(255),
    amount DECIMAL(10,2),
    status VARCHAR(50)
);
```

### 4. Notification Service (notification_db)
**Tabela: notification**
```sql
CREATE TABLE notification (
    notification_id VARCHAR(255) PRIMARY KEY,
    reservation_id VARCHAR(255),
    notification_type VARCHAR(50),
    status VARCHAR(50)
);
```

## 📝 APIs e Exemplos de Payloads

### 1. Saga Orchestrator Service

#### 🎯 POST `/api/orchestrator/reserve` - Iniciar Transação Saga

**Request:**
```bash
POST http://localhost:8080/api/orchestrator/reserve
?userId=user123
&journeyDate=2024-12-25
&seatNumber=A15
&amount=150.0
&simulateFailureAt=SEND_NOTIFICATION  # Opcional
```

**Response (Sucesso - HTTP 200):**
```json
{
  "reservation": {
    "reservationId": "uuid-123",
    "userId": "user123",
    "journeyDate": "2024-12-25",
    "status": "CREATED",
    "seatNumber": "A15",
    "amount": 150.0
  },
  "payment": {
    "paymentId": "uuid-456",
    "reservationId": "uuid-123",
    "amount": 150.0,
    "status": "APPROVED"
  },
  "notification": {
    "notificationId": "uuid-789",
    "reservationId": "uuid-123",
    "notificationType": "CONFIRMATION",
    "status": "SENT"
  },
  "completedSteps": ["CREATE_RESERVATION", "PROCESS_PAYMENT", "SEND_NOTIFICATION"],
  "failedStep": null,
  "failureReason": null,
  "success": true,
  "compensatedSteps": []
}
```

**Response (Falha - HTTP 409):**
```json
{
  "reservation": {
    "reservationId": "uuid-123",
    "userId": "user123",
    "journeyDate": "2024-12-25",
    "status": "CREATED",
    "seatNumber": "A15",
    "amount": 150.0
  },
  "payment": {
    "paymentId": "uuid-456",
    "reservationId": "uuid-123",
    "amount": 150.0,
    "status": "APPROVED"
  },
  "notification": {
    "notificationId": "uuid-789",
    "reservationId": "uuid-123",
    "notificationType": "CONFIRMATION",
    "status": "FAILED"
  },
  "completedSteps": ["CREATE_RESERVATION", "PROCESS_PAYMENT"],
  "failedStep": "SEND_NOTIFICATION",
  "failureReason": "Notification service failed to send confirmation",
  "success": false,
  "compensatedSteps": ["REFUND_PAYMENT", "CANCEL_RESERVATION"]
}
```

### 2. Reservation Service

#### 🎯 POST `/api/reservations` - Criar Reserva

**Request:**
```bash
POST http://localhost:8081/api/reservations
?userId=user123
&journeyDate=2024-12-25
&seatNumber=A15
&amount=150.0
```

**Response (Sucesso - HTTP 200):**
```json
{
  "reservationId": "uuid-123",
  "userId": "user123",
  "journeyDate": "2024-12-25",
  "status": "CREATED",
  "seatNumber": "A15",
  "amount": 150.0
}
```

**Response (Erro - HTTP 400):**
```json
{
  "code": "RESERVATION_INVALID_ARGUMENT",
  "message": "User ID cannot be null or empty",
  "timestamp": "2024-08-11T19:45:30.123",
  "path": "/api/reservations"
}
```

#### 🎯 POST `/api/reservations/{id}/cancel` - Cancelar Reserva

**Request:**
```bash
POST http://localhost:8081/api/reservations/uuid-123/cancel
```

**Response (Sucesso - HTTP 200):**
```json
{
  "reservationId": "uuid-123",
  "userId": "user123",
  "journeyDate": "2024-12-25",
  "status": "CANCELED",
  "seatNumber": "A15",
  "amount": 150.0
}
```

### 3. Payment Service

#### 🎯 POST `/api/payments` - Processar Pagamento

**Request:**
```bash
POST http://localhost:8082/api/payments
?reservationId=uuid-123
&amount=150.0
```

**Response (Sucesso - HTTP 200):**
```json
{
  "paymentId": "uuid-456",
  "reservationId": "uuid-123",
  "amount": 150.0,
  "status": "APPROVED"
}
```

**Response (Erro - HTTP 500):**
```json
{
  "code": "PAYMENT_PROCESSING_ERROR",
  "message": "Failed to process payment: Insufficient funds",
  "timestamp": "2024-08-11T19:45:30.123",
  "path": "/api/payments"
}
```

#### 🎯 POST `/api/payments/{id}/refund` - Estornar Pagamento

**Request:**
```bash
POST http://localhost:8082/api/payments/uuid-456/refund
```

**Response (Sucesso - HTTP 200):**
```json
{
  "paymentId": "uuid-456",
  "reservationId": "uuid-123",
  "amount": 150.0,
  "status": "REFUNDED"
}
```

### 4. Notification Service

#### 🎯 POST `/api/notifications/confirm` - Enviar Confirmação

**Request:**
```bash
POST http://localhost:8083/api/notifications/confirm
?reservationId=uuid-123
```

**Response (Sucesso - HTTP 200):**
```json
{
  "notificationId": "uuid-789",
  "reservationId": "uuid-123",
  "notificationType": "CONFIRMATION",
  "status": "SENT"
}
```

**Response (Erro - HTTP 500):**
```json
{
  "code": "NOTIFICATION_SEND_ERROR",
  "message": "Failed to send notification: Email service unavailable",
  "timestamp": "2024-08-11T19:45:30.123",
  "path": "/api/notifications/confirm"
}
```

#### 🎯 POST `/api/notifications/{id}/cancel` - Enviar Cancelamento

**Request:**
```bash
POST http://localhost:8083/api/notifications/uuid-789/cancel
```

**Response (Sucesso - HTTP 200):**
```json
{
  "notificationId": "uuid-789",
  "reservationId": "uuid-123",
  "notificationType": "CANCELLATION",
  "status": "SENT"
}
```

## 🔄 Fluxo da Transação Saga

### Cenário de Sucesso:
1. **CREATE_RESERVATION** → Cria reserva (Status: CREATED)
2. **PROCESS_PAYMENT** → Processa pagamento (Status: APPROVED)
3. **SEND_NOTIFICATION** → Envia confirmação (Status: SENT)

### Cenário de Falha (com Compensação):
1. **CREATE_RESERVATION** → ✅ Cria reserva (Status: CREATED)
2. **PROCESS_PAYMENT** → ✅ Processa pagamento (Status: APPROVED)
3. **SEND_NOTIFICATION** → ❌ Falha no envio (Status: FAILED)
4. **Compensação:**
   - **REFUND_PAYMENT** → Estorna pagamento (Status: REFUNDED)
   - **CANCEL_RESERVATION** → Cancela reserva (Status: CANCELED)

## 🧪 Testes

### Teste de Sucesso Completo:
```bash
POST http://localhost:8080/api/orchestrator/reserve
?userId=test123&journeyDate=2024-12-25&seatNumber=A1&amount=100.0
```

### Teste de Falha no Payment:
```bash
POST http://localhost:8080/api/orchestrator/reserve
?userId=test123&journeyDate=2024-12-25&seatNumber=A1&amount=100.0&simulateFailureAt=PROCESS_PAYMENT
```

### Teste de Falha na Notification:
```bash
POST http://localhost:8080/api/orchestrator/reserve
?userId=test123&journeyDate=2024-12-25&seatNumber=A1&amount=100.0&simulateFailureAt=SEND_NOTIFICATION
```

## 📋 Códigos de Status HTTP

| Código | Descrição | Quando Ocorre |
|--------|-----------|---------------|
| 200 | Success | Operação executada com sucesso |
| 400 | Bad Request | Argumentos inválidos |
| 409 | Conflict | Transação saga falhou mas foi compensada |
| 500 | Internal Server Error | Erro interno do serviço |

## 🛠️ Monitoramento

- **Logs**: Cada serviço produz logs detalhados sobre operações
- **Swagger UI**: Interface para testar APIs individualmente
- **Banco de Dados**: Consultar diretamente o estado persistido
- **Container Status**: `docker ps` para verificar containers ativos