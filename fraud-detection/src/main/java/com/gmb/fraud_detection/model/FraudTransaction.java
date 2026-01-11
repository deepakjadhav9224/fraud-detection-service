package com.gmb.fraud_detection.model;

import com.gmb.fraud_detection.constants.Constants;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionId;
    private String customerId;
    private Double amount;
    private String currency;
    private String channel;
    private String deviceId;
    private String location;
    private LocalDateTime timestamp;
    @Column(name = "riskscore", nullable = false)
    private Integer riskScore;
    @Enumerated(EnumType.STRING)
    private Constants.RiskLevel riskLevel;
    @Enumerated(EnumType.STRING)
    private Constants.SuggestedAction suggestedAction;
    @Enumerated(EnumType.STRING)
    private Constants.TransactionStatus status;
    private String comment;
}