package com.gmb.fraud_detection.service;

import com.gmb.fraud_detection.dto.FraudTransactionDto;

public interface FraudDetectionService {

    FraudTransactionDto evaluateTransaction(FraudTransactionDto fraudTransactionDto);
}
