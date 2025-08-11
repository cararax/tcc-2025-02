package com.carara.payment.controller;

import com.carara.payment.model.Payment;
import com.carara.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Controller", description = "API for managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Process a payment", description = "Processes a payment for a reservation", responses = {@ApiResponse(responseCode = "200", description = "Payment processed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class)))})
    public ResponseEntity<Payment> processPayment(@Parameter(description = "Reservation ID") @RequestParam String reservationId,
                                                  @Parameter(description = "Amount") @RequestParam Double amount) {
        return ResponseEntity.ok(paymentService.processPayment(reservationId, amount));
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment", description = "Refunds an existing payment by ID", responses = {@ApiResponse(responseCode = "200", description = "Payment refunded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class)))})
    public ResponseEntity<Payment> refundPayment(@Parameter(description = "Payment ID") @PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }
} 