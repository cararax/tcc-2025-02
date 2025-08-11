package com.carara.saga.controller;

import com.carara.saga.model.TransactionContext;
import com.carara.saga.service.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/orchestrator")
@Tag(name = "Orchestrator Controller", description = "API for orchestrating the Saga pattern flow")
public class OrchestratorController {

    private final OrchestratorService orchestratorService;

    @Autowired
    public OrchestratorController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/reserve")
    @Operation(
        summary = "Create a reservation with Saga pattern", 
        description = "Orchestrates the complete reservation flow using the Saga pattern", 
        responses = {
            @ApiResponse(responseCode = "200", description = "Reservation process completed successfully", 
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionContext.class))),
            @ApiResponse(responseCode = "409", description = "Reservation process failed with compensation details", 
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionContext.class)))
        }
    )
    public ResponseEntity<TransactionContext> createReservation(@Parameter(description = "User ID") @RequestParam String userId,
                                                                @Parameter(description = "Journey date") @RequestParam String journeyDate,
                                                                @Parameter(description = "Seat number") @RequestParam String seatNumber,
                                                                @Parameter(description = "Amount") @RequestParam Double amount,
                                                                @Parameter(description = "Simulate failure at step (optional): CREATE_RESERVATION, PROCESS_PAYMENT, SEND_NOTIFICATION") @RequestParam(required = false) String simulateFailureAt) {
        try {
            TransactionContext context = orchestratorService.executeTransaction(userId, journeyDate, seatNumber, amount, simulateFailureAt);
            
            if (context.isSuccess()) {
                return ResponseEntity.ok(context);
            } else {
                // Retorna 409 (Conflict) para indicar que a transação falhou mas foi compensada
                return ResponseEntity.status(409).body(context);
            }
        } catch (Exception e) {
            // Se por algum motivo uma exceção não tratada chegar até aqui,
            // cria um contexto de falha e retorna
            TransactionContext errorContext = new TransactionContext();
            errorContext.setFailure("UNKNOWN_ERROR", e.getMessage());
            return ResponseEntity.status(500).body(errorContext);
        }
    }
} 