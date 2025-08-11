package com.carara.notification.controller;

import com.carara.notification.model.Notification;
import com.carara.notification.service.NotificationService;
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
@RequestMapping("/api/notifications")
@Tag(name = "Notification Controller", description = "API for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/confirm")
    @Operation(summary = "Send confirmation notification", description = "Sends a confirmation notification for a reservation", responses = {@ApiResponse(responseCode = "200", description = "Notification sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class)))})
    public ResponseEntity<Notification> sendConfirmation(@Parameter(description = "Reservation ID") @RequestParam String reservationId) {
        return ResponseEntity.ok(notificationService.sendConfirmation(reservationId));
    }

    @PostMapping("/{notificationId}/cancel")
    @Operation(summary = "Send cancellation notification", description = "Sends a cancellation notification for an existing notification", responses = {@ApiResponse(responseCode = "200", description = "Cancellation notification sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class)))})
    public ResponseEntity<Notification> sendCancellation(@Parameter(description = "Notification ID") @PathVariable String notificationId) {
        return ResponseEntity.ok(notificationService.sendCancellation(notificationId));
    }
} 