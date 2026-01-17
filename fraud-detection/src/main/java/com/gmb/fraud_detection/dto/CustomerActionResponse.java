package com.gmb.fraud_detection.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerActionResponse {
    private String transactionId;
    private String status;      // e.g., "RECEIVED", "UNDER_INVESTIGATION"
    private String userMessage; // "We've received your report and frozen the transaction."
}