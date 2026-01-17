package com.gmb.fraud_detection.service.impl;

import com.gmb.fraud_detection.dto.*;
import com.gmb.fraud_detection.mapper.FraudTransactionMapper;
import com.gmb.fraud_detection.model.FraudTransaction;
import com.gmb.fraud_detection.repository.FraudTransactionRepository;
import com.gmb.fraud_detection.service.FraudDetectionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.gmb.fraud_detection.constants.Constants.*;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FraudDetectionImpl implements FraudDetectionService {

    private final FraudTransactionRepository repository;
    private final FraudTransactionMapper mapper;

    @Override
    public FraudEvaluationResponse evaluateTransaction(FraudTransactionDto requestDto) {
        FraudTransaction transaction = mapper.toEntity(requestDto);

        // 1. Calculate Score
        int score = calculateRiskScore(transaction);
        transaction.setRiskScore(score);

        // 2. Determine Level and Action based on Score
        if (score >= 80) {
            transaction.setRiskLevel(RiskLevel.HIGH);
            transaction.setSuggestedAction(SuggestedAction.REJECT);
            transaction.setStatus(TransactionStatus.BLOCKED);
        } else if (score >= 50) {
            transaction.setRiskLevel(RiskLevel.HIGH);
            transaction.setSuggestedAction(SuggestedAction.REVIEW);
            transaction.setStatus(TransactionStatus.PENDING_REVIEW);
        } else if (score >= 20) {
            transaction.setRiskLevel(RiskLevel.MEDIUM);
            transaction.setSuggestedAction(SuggestedAction.CHALLENGE);
            transaction.setStatus(TransactionStatus.PENDING_REVIEW);
        } else {
            transaction.setRiskLevel(RiskLevel.LOW);
            transaction.setSuggestedAction(SuggestedAction.APPROVE);
            transaction.setStatus(TransactionStatus.APPROVED);
        }

        // 3. Save to DB
        repository.save(transaction);

        // 4. Return enriched response
        return FraudEvaluationResponse.builder()
                .transactionId(transaction.getTransactionId())
                .riskScore(transaction.getRiskScore())
                .riskLevel(transaction.getRiskLevel())
                .suggestedAction(transaction.getSuggestedAction())
                .status(transaction.getStatus())
                .build();
    }

    private int calculateRiskScore(FraudTransaction transaction) {
        int score = 0;

        // Rule 1: High Amount
        if (transaction.getAmount() != null) {
            if (transaction.getAmount() > 50000) {
                score += 50;
            } else if (transaction.getAmount() > 10000) {
                score += 20;
            }
        }

        // Rule 2: Missing Device ID
        if (transaction.getDeviceId() == null || transaction.getDeviceId().isEmpty()) {
            score += 20;
        }

        // Rule 3: Suspicious Locations
        if (transaction.getLocation() != null && (transaction.getLocation().equalsIgnoreCase("Unknown") || transaction.getLocation().equalsIgnoreCase("HighRiskCountry"))) {
            score += 30;
        }

        // Rule 4: Channel based risk
        if ("WEB".equalsIgnoreCase(transaction.getChannel()) || "MOBILE".equalsIgnoreCase(transaction.getChannel())) {
            score += 10;
        }

        return Math.min(score, 100);
    }

    @Override
    public StatusUpdateResponse updateTransactionStatus(String transactionId, StatusUpdateRequest request) {
        // 1. Find the existing transaction
        FraudTransaction transaction = repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        // 2. Capture old status for the response
        TransactionStatus oldStatus = transaction.getStatus();

        // 3. Update status and metadata (Real world: you'd save 'reason' and 'updatedBy' to an audit table)
        transaction.setStatus(request.getStatus());

        // 4. Save changes
        repository.save(transaction);

        // 5. Return the response
        return StatusUpdateResponse.builder()
                .transactionId(transactionId)
                .oldStatus(oldStatus)
                .newStatus(transaction.getStatus())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public FraudTransactionResponse getTransaction(String transactionId) {
        // 1. Fetch from DB
        FraudTransaction tx = repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction ID not found: " + transactionId));

        // 2. Map Entity to Response DTO
        return FraudTransactionResponse.builder()
                .transactionId(tx.getTransactionId())
                .customerId(tx.getCustomerId())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .riskScore(tx.getRiskScore())
                .riskLevel(tx.getRiskLevel())
                .suggestedAction(tx.getSuggestedAction())
                .status(tx.getStatus())
                .timestamp(tx.getTimestamp())
                .build();
    }

    @Override
    @Transactional
    public CustomerActionResponse processCustomerConfirmation(CustomerActionRequest request) {
        // 1. Fetch the transaction
        FraudTransaction tx = repository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // 2. SECURITY CHECK: Does this transaction belong to the customer who is responding?
        if (!tx.getCustomerId().equals(request.getCustomerId())) {
            throw new SecurityException("Unauthorized: Customer ID mismatch.");
        }

        // 3. Update Status based on 'confirmed' boolean
        if (request.isConfirmed()) {
            tx.setStatus(TransactionStatus.APPROVED);
            tx.setComment("Customer confirmed: " + request.getComment());
        } else {
            tx.setStatus(TransactionStatus.BLOCKED);
            tx.setComment("Customer denied: " + request.getComment());
        }

        repository.save(tx);

        return CustomerActionResponse.builder()
                .transactionId(tx.getTransactionId())
                .status(tx.getStatus().name())
                .userMessage(request.isConfirmed() ? "Processing payment..." : "Card blocked for safety.")
                .build();
    }
}