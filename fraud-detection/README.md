# Fraud Detection Service - Kafka Integration

This service now includes Apache Kafka integration for event-driven communication.

## Kafka Topics

| Topic Name | Description |
|------------|-------------|
| `transaction-evaluated` | Published when a transaction is evaluated by the system. |
| `transaction-status-updated` | Published when an analyst manually updates a transaction status. |
| `customer-action-received` | Published when a customer confirms or denies a transaction. |

## Event Payload

All events use the following JSON structure:

```json
{
  "transactionId": 12345,
  "previousStatus": "PENDING_REVIEW",
  "currentStatus": "BLOCKED",
  "riskScore": 85,
  "actor": "ANALYST",
  "timestamp": "2023-10-27T10:00:00"
}
```

## Local Setup

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven

### Running Kafka Locally

Use the provided `docker-compose.yml` to start Kafka and Zookeeper:

```bash
docker-compose up -d
```

### Testing Events

You can use the Kafka console consumer to verify events are being published.

**Listen to `transaction-evaluated`:**
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic transaction-evaluated --from-beginning
```

**Listen to `transaction-status-updated`:**
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic transaction-status-updated --from-beginning
```

**Listen to `customer-action-received`:**
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic customer-action-received --from-beginning
```

## Configuration

Kafka configuration is located in `src/main/resources/application.yml`.

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: fraud-detection-group
      auto-offset-reset: earliest
```
