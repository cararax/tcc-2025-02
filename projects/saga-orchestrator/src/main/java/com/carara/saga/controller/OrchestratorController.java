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
    @Operation(summary = "Create a reservation with Saga pattern", description = "Orchestrates the complete reservation flow using the Saga pattern", responses = {@ApiResponse(responseCode = "200", description = "Reservation process completed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionContext.class)))})
    public ResponseEntity<TransactionContext> createReservation(@Parameter(description = "User ID") @RequestParam String userId,
                                                                @Parameter(description = "Journey date") @RequestParam String journeyDate,
                                                                @Parameter(description = "Seat number") @RequestParam String seatNumber,
                                                                @Parameter(description = "Amount") @RequestParam Double amount) {
        return ResponseEntity.ok(orchestratorService.executeTransaction(userId, journeyDate, seatNumber, amount));
    }
} 