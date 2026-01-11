package com.gmb.fraud_detection.controller;

import com.gmb.fraud_detection.dto.FraudTransactionDto;
import com.gmb.fraud_detection.service.FraudDetectionService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/fraud-transactions")
public class FraudTransactionController {

    private FraudDetectionService fraudDetectionService;

    @PostMapping("/evaluate")
    public ResponseEntity<FraudTransactionDto> evaluateTransaction(@RequestBody FraudTransactionDto fraudTransactionDto) {
        FraudTransactionDto result = fraudDetectionService.evaluateTransaction(fraudTransactionDto);
        return ResponseEntity.ok(result);
    }
}
