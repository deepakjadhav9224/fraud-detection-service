package com.gmb.fraud_detection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FraudTransactionDto {
    private String transactionId;
    private String customerId;
    private Double amount;
    private String currency;
    private String channel;
    private String deviceId;
    private String location;
    private Instant timestamp;
    private int riskScore;
}


