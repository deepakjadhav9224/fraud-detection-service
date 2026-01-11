package com.gmb.fraud_detection.service.impl;

import com.gmb.fraud_detection.dto.FraudTransactionDto;
import com.gmb.fraud_detection.mapper.FraudTransactionMapper;
import com.gmb.fraud_detection.model.FraudTransaction;
import com.gmb.fraud_detection.repository.FraudTransactionRepository;
import com.gmb.fraud_detection.service.FraudDetectionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class FraudDetectionImpl implements FraudDetectionService {

    private FraudTransactionRepository fraudTransactionRepository;
    @Override
    public FraudTransactionDto evaluateTransaction(FraudTransactionDto fraudTransactionDto) {
        FraudTransaction fraudTransaction = FraudTransactionMapper.mapToFraudTransaction(fraudTransactionDto);
        FraudTransaction savedTransaction = fraudTransactionRepository.save(fraudTransaction);
        return FraudTransactionMapper.mapToFraudTransactionDto(savedTransaction);
    }

    private int calculateRiskScore(
            FraudTransactionDto request) {

        double score = 0;
        if (request.getAmount() > 100000) score += 70;
        return (int) Math.min(score, 100);
    }
}
