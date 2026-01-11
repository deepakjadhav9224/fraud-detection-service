package com.gmb.fraud_detection.mapper;

import com.gmb.fraud_detection.dto.FraudTransactionDto;
import com.gmb.fraud_detection.model.FraudTransaction;

public class FraudTransactionMapper {

    public static FraudTransactionDto mapToFraudTransactionDto(FraudTransaction fraudTransaction) {
        if (fraudTransaction == null) {
            return null;
        }
        return new FraudTransactionDto(
                fraudTransaction.getTransactionId(),
                fraudTransaction.getCustomerId(),
                fraudTransaction.getAmount(),
                fraudTransaction.getCurrency(),
                fraudTransaction.getChannel(),
                fraudTransaction.getDeviceId(),
                fraudTransaction.getLocation(),
                fraudTransaction.getTimestamp(),
                fraudTransaction.getRiskScore()
        );
    }

    public static FraudTransaction mapToFraudTransaction(FraudTransactionDto fraudTransactionDto) {
        if (fraudTransactionDto == null) {
            return null;
        }
        return new FraudTransaction(
                fraudTransactionDto.getTransactionId(),
                fraudTransactionDto.getCustomerId(),
                fraudTransactionDto.getAmount(),
                fraudTransactionDto.getCurrency(),
                fraudTransactionDto.getChannel(),
                fraudTransactionDto.getDeviceId(),
                fraudTransactionDto.getLocation(),
                fraudTransactionDto.getTimestamp(),
                fraudTransactionDto.getRiskScore()
        );
    }
}
