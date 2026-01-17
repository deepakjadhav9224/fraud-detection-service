package com.gmb.fraud_detection.dto;

import com.gmb.fraud_detection.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudEvaluationResponse {
    private String transactionId;
    private Integer riskScore;
    private Constants.RiskLevel riskLevel;
    private Constants.SuggestedAction suggestedAction;
    private Constants.TransactionStatus status;
}