package com.gmb.fraud_detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudEventDto {
    private Long transactionId;
    private String previousStatus;
    private String currentStatus;
    private Integer riskScore;
    private String actor; // SYSTEM / ANALYST / CUSTOMER
    private LocalDateTime timestamp;
}
