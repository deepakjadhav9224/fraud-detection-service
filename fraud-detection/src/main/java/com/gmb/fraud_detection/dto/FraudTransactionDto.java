package com.gmb.fraud_detection.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FraudTransactionDto {
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    private Double amount;
    @NotBlank(message = "Currency is required")
    private String currency;
    private String channel;
    private String deviceId;
    private String location;
    private LocalDateTime timestamp;
}