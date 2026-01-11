package com.gmb.fraud_detection.service;

import com.gmb.fraud_detection.dto.*;
import com.gmb.fraud_detection.model.FraudTransaction;

public interface FraudDetectionService {

    FraudEvaluationResponse evaluateTransaction(FraudTransactionDto fraudTransactionDto);
    StatusUpdateResponse updateTransactionStatus(String transactionId, StatusUpdateRequest request);
    FraudTransactionResponse getTransaction(String transactionId);
    CustomerActionResponse processCustomerConfirmation(CustomerActionRequest request);
}
