package com.gmb.fraud_detection.controller;

import com.gmb.fraud_detection.dto.*;
import com.gmb.fraud_detection.service.FraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/fraud-detection")
@Tag(name = "Fraud Detection", description = "Endpoints for evaluating transactions and managing fraud alerts")
public class FraudTransactionController {

    private FraudDetectionService fraudDetectionService;

    @Operation(summary = "Evaluate a transaction", description = "Analyzes a transaction for potential fraud based on risk rules.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction evaluated successfully",
                    content = @Content(schema = @Schema(implementation = FraudEvaluationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/evaluate")
    public ResponseEntity<FraudEvaluationResponse> evaluateTransaction(@RequestBody @Valid FraudTransactionDto fraudTransactionDto) {
        FraudEvaluationResponse result = fraudDetectionService.evaluateTransaction(fraudTransactionDto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Update transaction status", description = "Manually updates the status of a transaction (e.g., by an analyst).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = StatusUpdateResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @PatchMapping("/transactions/{transactionId}/update-status")
    public ResponseEntity<StatusUpdateResponse> updateStatus(
            @PathVariable String transactionId,
            @RequestBody StatusUpdateRequest request) {

        StatusUpdateResponse response = fraudDetectionService.updateTransactionStatus(transactionId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get transaction details", description = "Retrieves the details and fraud analysis result of a specific transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = FraudTransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<FraudTransactionResponse> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(fraudDetectionService.getTransaction(transactionId));
    }

    @Operation(summary = "Process customer confirmation", description = "Handles the customer's response (confirm/deny) to a fraud alert.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer action processed",
                    content = @Content(schema = @Schema(implementation = CustomerActionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Customer ID mismatch)")
    })
    @PostMapping("/customer/confirm")
    public ResponseEntity<CustomerActionResponse> verifyTransaction(@RequestBody CustomerActionRequest request) {
        return ResponseEntity.ok(fraudDetectionService.processCustomerConfirmation(request));
    }
}
