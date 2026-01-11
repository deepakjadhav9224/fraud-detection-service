package com.gmb.fraud_detection.repository;

import com.gmb.fraud_detection.model.FraudTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudTransactionRepository extends JpaRepository<FraudTransaction, String> {
}
