package com.gmb.fraud_detection.dto;

import lombok.Data;

@Data
public class CustomerActionRequest {
    private String customerId;
    private String transactionId;
    private boolean confirmed; // true = "Yes, it was me", false = "No, not me"
    private String comment;
}