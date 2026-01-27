package com.gmb.fraud_detection.service;

import com.gmb.fraud_detection.dto.FraudEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "${app.kafka.topics.transaction-evaluated}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTransactionEvaluated(FraudEventDto event) {
        log.info("Received Transaction Evaluated Event: {}", event);
        // Simulate downstream service (e.g., Notification)
        log.info("Simulating Notification Service for Transaction ID: {}", event.getTransactionId());
    }

    @KafkaListener(topics = "${app.kafka.topics.transaction-status-updated}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTransactionStatusUpdated(FraudEventDto event) {
        log.info("Received Transaction Status Updated Event: {}", event);
        // Simulate downstream service (e.g., Audit)
        log.info("Simulating Audit Service for Transaction ID: {}", event.getTransactionId());
    }

    @KafkaListener(topics = "${app.kafka.topics.customer-action-received}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCustomerActionReceived(FraudEventDto event) {
        log.info("Received Customer Action Event: {}", event);
        // Simulate downstream service (e.g., AML)
        log.info("Simulating AML Service for Transaction ID: {}", event.getTransactionId());
    }
}
