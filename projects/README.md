# Arquitetura de Microsserviços com Padrão Saga (Orquestração)

Este projeto demonstra a implementação do padrão Saga, utilizando o modelo de **Orquestração**, para gerenciar transações distribuídas em uma arquitetura de microsserviços. A comunicação entre os serviços é realizada de forma assíncrona através do **RabbitMQ**.

A aplicação simula um sistema de reserva de passagens (`ReservaExpress`), composto pelos seguintes serviços:

- **Saga Orchestrator (`saga-orchestrator`):** O serviço central que gerencia o ciclo de vida da transação. Ele é responsável por enviar comandos para os outros serviços e reagir às suas respostas, orquestrando o fluxo da saga e executando compensações em caso de falha.
- **Reservation Service (`reservation-service`):** Gerencia a criação e o cancelamento de reservas.
- **Payment Service (`payment-service`):** Processa pagamentos e estornos.
- **Notification Service (`notification-service`):** Envia notificações de confirmação para o usuário.

## Arquitetura e Padrão Saga

A arquitetura segue o princípio de **banco de dados por serviço**, onde cada microsserviço possui sua própria instância do PostgreSQL, garantindo baixo acoplamento.

O fluxo da transação é o seguinte:

1.  Uma requisição HTTP é enviada ao `Orchestrator Controller` para iniciar uma nova reserva.
2.  O `Orchestrator Service` cria uma nova entidade `Saga` com o status `STARTED` e a persiste em seu banco de dados.
3.  O orquestrador inicia a saga enviando uma mensagem de comando **assíncrona** (`CREATE_RESERVATION`) para a fila do `reservation-service` via RabbitMQ.
4.  O `ReservationListener` no `reservation-service` consome a mensagem, processa a reserva e envia uma mensagem de **resposta** (`SUCCEEDED` ou `FAILED`) de volta para o orquestrador.
5.  O `SagaListener` no orquestrador recebe a resposta.
    - Se **sucesso**, ele atualiza o estado da saga e envia o próximo comando (`PROCESS_PAYMENT`) para o `payment-service`.
    - Se **falha**, ele inicia o processo de **compensação**.
6.  O fluxo continua até que o último passo (`SEND_NOTIFICATION`) seja concluído com sucesso, momento em que a saga é marcada como `SUCCEEDED`.

### Tratamento de Falhas e Compensação

Se qualquer passo da saga falhar, o orquestrador recebe uma resposta de `FAILED` e inicia a compensação, enviando comandos de compensação em ordem inversa para os serviços que já completaram suas transações. Por exemplo, se o pagamento falhar, o orquestrador enviará um comando `CANCEL_RESERVATION` para o `reservation-service`.

## Como Executar o Projeto

### Pré-requisitos

- Docker e Docker Compose
- JDK 17 ou superior
- Maven

### Passos

1.  **Construir os Projetos:**
    Navegue até o diretório de cada microsserviço (`saga-orchestrator`, `reservation-service`, `payment-service`, `notification-service`) e execute o comando Maven para construir o projeto:
    ```bash
    mvn clean install
    ```

2.  **Iniciar a Infraestrutura e os Serviços:**
    No diretório `projects/microservice`, execute o Docker Compose para iniciar os bancos de dados e o RabbitMQ:
    ```bash
    docker-compose up -d
    ```
    A interface de gerenciamento do RabbitMQ estará disponível em `http://localhost:15672` (guest/guest).

3.  **Executar os Microsserviços:**
    Inicie cada um dos quatro microsserviços Java. Você pode executá-los a partir da sua IDE ou via linha de comando com o Spring Boot Maven plugin:
    ```bash
    # Exemplo para o saga-orchestrator
    cd saga-orchestrator
    mvn spring-boot:run
    ```
    Repita o processo para os outros três serviços em terminais separados.

4.  **Iniciar uma Transação:**
    Use a interface do Swagger UI do `saga-orchestrator` para enviar uma requisição e iniciar o processo de reserva:
    - **URL:** `http://localhost:8080/swagger-ui.html`
    - **Endpoint:** `POST /api/orchestrator/reserve`

    Forneça os parâmetros necessários (`userId`, `journeyDate`, `seatNumber`, `amount`) e execute a requisição. A resposta será imediata, contendo o `transactionId`. Você pode acompanhar o fluxo da saga pelos logs de cada serviço no console.
