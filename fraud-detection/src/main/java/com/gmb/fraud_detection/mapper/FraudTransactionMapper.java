package com.gmb.fraud_detection.mapper;

import com.gmb.fraud_detection.dto.FraudTransactionDto;
import com.gmb.fraud_detection.model.FraudTransaction;
import org.springframework.stereotype.Component;

@Component
public class FraudTransactionMapper {

    public FraudTransaction toEntity(FraudTransactionDto dto) {
        FraudTransaction entity = new FraudTransaction();
        entity.setTransactionId(dto.getTransactionId());
        entity.setCustomerId(dto.getCustomerId());
        entity.setAmount(dto.getAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setChannel(dto.getChannel());
        entity.setDeviceId(dto.getDeviceId());
        entity.setLocation(dto.getLocation());
        entity.setTimestamp(dto.getTimestamp());
        return entity;
    }

    public FraudTransactionDto toDto(FraudTransaction entity) {
        FraudTransactionDto dto = new FraudTransactionDto();
        dto.setTransactionId(entity.getTransactionId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setChannel(entity.getChannel());
        dto.setDeviceId(entity.getDeviceId());
        dto.setLocation(entity.getLocation());
        dto.setTimestamp(entity.getTimestamp());
        return dto;
    }
}