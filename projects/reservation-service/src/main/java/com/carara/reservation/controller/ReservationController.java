package com.carara.reservation.controller;

import com.carara.reservation.model.Reservation;
import com.carara.reservation.service.ReservationService;
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
@RequestMapping("/api/reservations")
@Tag(name = "Reservation Controller", description = "API for managing reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create a new reservation", description = "Creates a new reservation with the provided details", responses = {@ApiResponse(responseCode = "200", description = "Reservation created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reservation.class)))})
    public ResponseEntity<Reservation> createReservation(@Parameter(description = "User ID") @RequestParam String userId,
                                                         @Parameter(description = "Journey date") @RequestParam String journeyDate,
                                                         @Parameter(description = "Seat number") @RequestParam String seatNumber,
                                                         @Parameter(description = "Amount") @RequestParam Double amount) {
        return ResponseEntity.ok(reservationService.createReservation(userId, journeyDate, seatNumber, amount));
    }

    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "Cancel a reservation", description = "Cancels an existing reservation by ID", responses = {@ApiResponse(responseCode = "200", description = "Reservation canceled successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reservation.class)))})
    public ResponseEntity<Reservation> cancelReservation(@Parameter(description = "Reservation ID") @PathVariable String reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
    }
} 