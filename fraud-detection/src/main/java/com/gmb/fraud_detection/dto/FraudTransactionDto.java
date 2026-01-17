package com.gmb.fraud_detection.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FraudTransactionDto {
    private String transactionId;
    private String customerId;
    private Double amount;
    private String currency;
    private String channel;
    private String deviceId;
    private String location;
    private LocalDateTime timestamp;
}