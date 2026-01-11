package com.gmb.fraud_detection.dto;

import com.gmb.fraud_detection.constants.Constants;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class StatusUpdateResponse {
    private String transactionId;
    private Constants.TransactionStatus oldStatus;
    private Constants.TransactionStatus newStatus;
    private LocalDateTime updatedAt;
}