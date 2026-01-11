package com.gmb.fraud_detection.constants;

public class Constants {

    public enum RiskLevel { LOW, MEDIUM, HIGH }

    public enum SuggestedAction { APPROVE, CHALLENGE, REVIEW, REJECT }

    public enum TransactionStatus { PENDING_REVIEW, APPROVED, FLAGGED, BLOCKED }
}
