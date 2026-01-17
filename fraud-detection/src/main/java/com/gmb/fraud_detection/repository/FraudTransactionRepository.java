package com.gmb.fraud_detection.repository;

import com.gmb.fraud_detection.model.FraudTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FraudTransactionRepository extends JpaRepository<FraudTransaction, String> {
    Optional<FraudTransaction> findByTransactionId(String transactionId);
}
