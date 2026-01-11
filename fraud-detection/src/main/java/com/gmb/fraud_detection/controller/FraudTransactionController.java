package com.gmb.fraud_detection.controller;

import com.gmb.fraud_detection.dto.*;
import com.gmb.fraud_detection.service.FraudDetectionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/fraud-detection")
public class FraudTransactionController {

    private FraudDetectionService fraudDetectionService;

    @PostMapping("/evaluate")
    public ResponseEntity<FraudEvaluationResponse> evaluateTransaction(@RequestBody FraudTransactionDto fraudTransactionDto) {
        FraudEvaluationResponse result = fraudDetectionService.evaluateTransaction(fraudTransactionDto);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/transactions/{transactionId}/status")
    public ResponseEntity<StatusUpdateResponse> updateStatus(
            @PathVariable String transactionId,
            @RequestBody StatusUpdateRequest request) {

        StatusUpdateResponse response = fraudDetectionService.updateTransactionStatus(transactionId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<FraudTransactionResponse> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(fraudDetectionService.getTransaction(transactionId));
    }

    @PostMapping("/customer/verify-transaction")
    public ResponseEntity<CustomerActionResponse> verifyTransaction(@RequestBody CustomerActionRequest request) {
        return ResponseEntity.ok(fraudDetectionService.processCustomerConfirmation(request));
    }
}
