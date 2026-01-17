package com.gmb.fraud_detection.dto;

import com.gmb.fraud_detection.constants.Constants;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FraudTransactionResponse {
    private String transactionId;
    private String customerId;
    private Double amount;
    private String currency;

    private Integer riskScore;
    private Constants.RiskLevel riskLevel;
    private Constants.SuggestedAction suggestedAction;

    private Constants.TransactionStatus status;
    private LocalDateTime timestamp;
}