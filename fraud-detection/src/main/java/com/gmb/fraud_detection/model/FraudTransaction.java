package com.gmb.fraud_detection.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fraud_transactions")
public class FraudTransaction {

    @Id
    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(nullable = false)
    private Double amount;

    @Column(length = 10)
    private String currency;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(name = "device_id", length = 50)
    private String deviceId;

    @Column(length = 10)
    private String location;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "riskscore", nullable = false)
    private int riskScore;
}
