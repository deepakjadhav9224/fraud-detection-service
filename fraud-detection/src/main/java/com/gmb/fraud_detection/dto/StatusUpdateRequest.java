package com.gmb.fraud_detection.dto;

import com.gmb.fraud_detection.constants.Constants;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private Constants.TransactionStatus status;
    private String reason;
    private String updatedBy;
}