package com.gmb.fraud_detection.service.impl;

import com.gmb.fraud_detection.constants.Constants;
import com.gmb.fraud_detection.dto.*;
import com.gmb.fraud_detection.mapper.FraudTransactionMapper;
import com.gmb.fraud_detection.model.FraudTransaction;
import com.gmb.fraud_detection.repository.FraudTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionImplTest {

    @Mock
    private FraudTransactionRepository repository;

    @Mock
    private FraudTransactionMapper mapper;

    @InjectMocks
    private FraudDetectionImpl fraudDetectionService;

    private FraudTransactionDto transactionDto;
    private FraudTransaction transaction;

    @BeforeEach
    void setUp() {
        transactionDto = new FraudTransactionDto();
        transactionDto.setTransactionId("TXN123");
        transactionDto.setCustomerId("CUST001");
        transactionDto.setAmount(5000.0);
        transactionDto.setCurrency("USD");
        transactionDto.setChannel("ATM");
        transactionDto.setDeviceId("DEV001");
        transactionDto.setLocation("New York");
        transactionDto.setTimestamp(LocalDateTime.now());

        transaction = FraudTransaction.builder()
                .transactionId("TXN123")
                .customerId("CUST001")
                .amount(5000.0)
                .currency("USD")
                .channel("ATM")
                .deviceId("DEV001")
                .location("New York")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void evaluateTransaction_LowRisk_ShouldApprove() {
        when(mapper.toEntity(transactionDto)).thenReturn(transaction);
        when(repository.save(any(FraudTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FraudEvaluationResponse response = fraudDetectionService.evaluateTransaction(transactionDto);

        assertEquals(Constants.RiskLevel.LOW, response.getRiskLevel());
        assertEquals(Constants.SuggestedAction.APPROVE, response.getSuggestedAction());
        assertEquals(Constants.TransactionStatus.APPROVED, response.getStatus());
        verify(repository).save(any(FraudTransaction.class));
    }

    @Test
    void evaluateTransaction_MediumRisk_ShouldChallenge() {
        transactionDto.setAmount(15000.0);
        transaction.setAmount(15000.0);
        
        when(mapper.toEntity(transactionDto)).thenReturn(transaction);
        when(repository.save(any(FraudTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FraudEvaluationResponse response = fraudDetectionService.evaluateTransaction(transactionDto);

        assertEquals(Constants.RiskLevel.MEDIUM, response.getRiskLevel());
        assertEquals(Constants.SuggestedAction.CHALLENGE, response.getSuggestedAction());
        assertEquals(Constants.TransactionStatus.PENDING_REVIEW, response.getStatus());
    }

    @Test
    void evaluateTransaction_HighRisk_ShouldReview() {
        transactionDto.setAmount(60000.0);
        transaction.setAmount(60000.0);

        when(mapper.toEntity(transactionDto)).thenReturn(transaction);
        when(repository.save(any(FraudTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FraudEvaluationResponse response = fraudDetectionService.evaluateTransaction(transactionDto);

        assertEquals(Constants.RiskLevel.HIGH, response.getRiskLevel());
        assertEquals(Constants.SuggestedAction.REVIEW, response.getSuggestedAction());
        assertEquals(Constants.TransactionStatus.PENDING_REVIEW, response.getStatus());
    }

    @Test
    void evaluateTransaction_VeryHighRisk_ShouldBlock() {
        transactionDto.setAmount(60000.0);
        transactionDto.setLocation("Unknown");
        
        transaction.setAmount(60000.0);
        transaction.setLocation("Unknown");

        when(mapper.toEntity(transactionDto)).thenReturn(transaction);
        when(repository.save(any(FraudTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FraudEvaluationResponse response = fraudDetectionService.evaluateTransaction(transactionDto);

        assertEquals(Constants.RiskLevel.HIGH, response.getRiskLevel());
        assertEquals(Constants.SuggestedAction.REJECT, response.getSuggestedAction());
        assertEquals(Constants.TransactionStatus.BLOCKED, response.getStatus());
    }

    @Test
    void updateTransactionStatus_ShouldUpdateStatus() {
        String txnId = "TXN123";
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(Constants.TransactionStatus.APPROVED);
        
        transaction.setStatus(Constants.TransactionStatus.PENDING_REVIEW);

        when(repository.findByTransactionId(txnId)).thenReturn(Optional.of(transaction));
        when(repository.save(any(FraudTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StatusUpdateResponse response = fraudDetectionService.updateTransactionStatus(txnId, request);

        assertEquals(Constants.TransactionStatus.PENDING_REVIEW, response.getOldStatus());
        assertEquals(Constants.TransactionStatus.APPROVED, response.getNewStatus());
        assertEquals(Constants.TransactionStatus.APPROVED, transaction.getStatus());
    }

    @Test
    void getTransaction_ShouldReturnTransaction() {
        String txnId = "TXN123";
        when(repository.findByTransactionId(txnId)).thenReturn(Optional.of(transaction));

        FraudTransactionResponse response = fraudDetectionService.getTransaction(txnId);

        assertEquals(txnId, response.getTransactionId());
        assertEquals(transaction.getAmount(), response.getAmount());
    }

    @Test
    void processCustomerConfirmation_Confirmed_ShouldApprove() {
        CustomerActionRequest request = new CustomerActionRequest();
        request.setTransactionId("TXN123");
        request.setCustomerId("CUST001");
        request.setConfirmed(true);
        request.setComment("Yes it was me");

        transaction.setStatus(Constants.TransactionStatus.PENDING_REVIEW);

        when(repository.findByTransactionId("TXN123")).thenReturn(Optional.of(transaction));
        when(repository.save(any(FraudTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerActionResponse response = fraudDetectionService.processCustomerConfirmation(request);

        assertEquals("APPROVED", response.getStatus());
        assertEquals(Constants.TransactionStatus.APPROVED, transaction.getStatus());
        assertTrue(transaction.getComment().contains("Customer confirmed"));
    }

    @Test
    void processCustomerConfirmation_Denied_ShouldBlock() {
        CustomerActionRequest request = new CustomerActionRequest();
        request.setTransactionId("TXN123");
        request.setCustomerId("CUST001");
        request.setConfirmed(false);
        request.setComment("Not me");

        transaction.setStatus(Constants.TransactionStatus.PENDING_REVIEW);

        when(repository.findByTransactionId("TXN123")).thenReturn(Optional.of(transaction));
        when(repository.save(any(FraudTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerActionResponse response = fraudDetectionService.processCustomerConfirmation(request);

        assertEquals("BLOCKED", response.getStatus());
        assertEquals(Constants.TransactionStatus.BLOCKED, transaction.getStatus());
        assertTrue(transaction.getComment().contains("Customer denied"));
    }
    
    @Test
    void processCustomerConfirmation_WrongCustomer_ShouldThrowException() {
        CustomerActionRequest request = new CustomerActionRequest();
        request.setTransactionId("TXN123");
        request.setCustomerId("WRONG_CUST");

        when(repository.findByTransactionId("TXN123")).thenReturn(Optional.of(transaction));

        assertThrows(SecurityException.class, () -> fraudDetectionService.processCustomerConfirmation(request));
    }
}
