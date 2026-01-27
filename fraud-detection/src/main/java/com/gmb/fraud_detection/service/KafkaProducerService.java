package com.gmb.fraud_detection.service;

import com.gmb.fraud_detection.dto.FraudEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Timeout for synchronous Kafka send (in seconds)
    private static final long SEND_TIMEOUT_SECONDS = 10;

    public void sendEvent(String topic, FraudEventDto event) {
        log.info("Publishing event to topic {}: {}", topic, event);
        try {
            // Synchronous send with timeout to fail fast if broker is unavailable
            var sendResult = kafkaTemplate.send(topic, String.valueOf(event.getTransactionId()), event)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info("Event sent successfully to topic {} with offset {}",
                    topic, sendResult.getRecordMetadata().offset());
        } catch (Exception e) {
            // Propagate error to caller so transaction can be rolled back
            log.error("Error publishing event to topic {} after {} seconds timeout",
                    topic, SEND_TIMEOUT_SECONDS, e);
            throw new RuntimeException("Failed to publish event to Kafka topic: " + topic, e);
        }
    }
}
