package com.carara.saga.service;

import com.carara.saga.model.NotificationDto;
import com.carara.saga.model.PaymentDto;
import com.carara.saga.model.ReservationDto;
import com.carara.saga.model.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class OrchestratorService {
    private final RestTemplate restTemplate;
    private final String reservationServiceUrl;
    private final String paymentServiceUrl;
    private final String notificationServiceUrl;
    
    public OrchestratorService(
            @Value("${services.reservation.url}") String reservationServiceUrl,
            @Value("${services.payment.url}") String paymentServiceUrl,
            @Value("${services.notification.url}") String notificationServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.reservationServiceUrl = reservationServiceUrl;
        this.paymentServiceUrl = paymentServiceUrl;
        this.notificationServiceUrl = notificationServiceUrl;
        log.info("OrchestratorService initialized with URLs - Reservation: {}, Payment: {}, Notification: {}", 
                reservationServiceUrl, paymentServiceUrl, notificationServiceUrl);
    }
    
    public TransactionContext executeTransaction(String userId, String journeyDate,
                                              String seatNumber, Double amount) {
        return executeTransaction(userId, journeyDate, seatNumber, amount, null);
    }
    
    public TransactionContext executeTransaction(String userId, String journeyDate,
                                              String seatNumber, Double amount, String simulateFailureAt) {
        log.info("Starting transaction execution for user {} - Journey: {}, Seat: {}, Amount: {}", 
                userId, journeyDate, seatNumber, amount);
        
        TransactionContext context = new TransactionContext();
        String currentStep = null;
        
        try {
            // Step 1: Create Reservation
            currentStep = "CREATE_RESERVATION";
            log.info("Step 1: Creating reservation");
            if (currentStep.equals(simulateFailureAt)) {
                // Criar uma reservation com status FAILED para mostrar no contexto
                ReservationDto failedReservation = new ReservationDto();
                failedReservation.setReservationId(java.util.UUID.randomUUID().toString());
                failedReservation.setUserId(userId);
                failedReservation.setJourneyDate(journeyDate);
                failedReservation.setSeatNumber(seatNumber);
                failedReservation.setAmount(amount);
                failedReservation.setStatus("FAILED");
                context.setReservation(failedReservation);
                throw new RuntimeException("Reservation service failed to create reservation");
            }
            ReservationDto reservation = createReservation(userId, journeyDate, seatNumber, amount);
            context.setReservation(reservation);
            context.addCompletedStep(currentStep);
            log.info("Reservation created successfully - ID: {}", reservation.getReservationId());
            
            // Step 2: Process Payment
            currentStep = "PROCESS_PAYMENT";
            log.info("Step 2: Processing payment");
            if (currentStep.equals(simulateFailureAt)) {
                // Criar um payment com status FAILED para mostrar no contexto
                PaymentDto failedPayment = new PaymentDto();
                failedPayment.setPaymentId(java.util.UUID.randomUUID().toString());
                failedPayment.setReservationId(context.getReservation().getReservationId());
                failedPayment.setAmount(amount);
                failedPayment.setStatus("FAILED");
                context.setPayment(failedPayment);
                throw new RuntimeException("Payment service failed to process payment");
            }
            PaymentDto payment = processPayment(reservation.getReservationId(), amount);
            context.setPayment(payment);
            context.addCompletedStep(currentStep);
            log.info("Payment processed successfully - ID: {}", payment.getPaymentId());
            
            // Step 3: Send Notification
            currentStep = "SEND_NOTIFICATION";
            log.info("Step 3: Sending confirmation");
            if (currentStep.equals(simulateFailureAt)) {
                // Para uma falha mais realística, vamos criar um notification com status FAILED
                NotificationDto failedNotification = new NotificationDto();
                failedNotification.setNotificationId(java.util.UUID.randomUUID().toString());
                failedNotification.setReservationId(context.getReservation().getReservationId());
                failedNotification.setNotificationType("CONFIRMATION");
                failedNotification.setStatus("FAILED");
                context.setNotification(failedNotification);
                throw new RuntimeException("Notification service failed to send confirmation");
            }
            NotificationDto notification = sendConfirmation(context.getReservation().getReservationId());
            context.setNotification(notification);
            context.addCompletedStep(currentStep);
            log.info("Confirmation sent successfully - ID: {}", notification.getNotificationId());
            
            // Mark transaction as successful
            context.markAsSuccess();
            log.info("Transaction completed successfully for user {}", userId);
            return context;
            
        } catch (Exception e) {
            log.error("Transaction failed at step {} for user {} - Error: {}", currentStep, userId, e.getMessage(), e);
            
            try {
                // Set failure information
                context.setFailure(currentStep, e.getMessage());
                
                log.info("Starting compensation process");
                executeCompensation(context);
                
                log.info("Compensation completed for failed transaction. Returning context with failure details.");
                return context; // Retorna o contexto em vez de lançar exceção
                
            } catch (Exception compensationError) {
                log.error("Error during compensation process: {}", compensationError.getMessage(), compensationError);
                // Mesmo se a compensação falhar, retorna o contexto com as informações disponíveis
                context.setFailure(currentStep, e.getMessage() + " | Compensation also failed: " + compensationError.getMessage());
                return context;
            }
        }
    }
    
    private void executeCompensation(TransactionContext context) {
        // Execute compensation in reverse order
        if (context.getPayment() != null) {
            try {
                log.info("Compensating payment - ID: {}", context.getPayment().getPaymentId());
                refundPayment(context.getPayment().getPaymentId());
                context.addCompensatedStep("REFUND_PAYMENT");
                log.info("Payment compensation completed successfully");
            } catch (Exception e) {
                log.error("Failed to compensate payment: {}", e.getMessage());
            }
        }
        
        if (context.getReservation() != null) {
            try {
                log.info("Compensating reservation - ID: {}", context.getReservation().getReservationId());
                cancelReservation(context.getReservation().getReservationId());
                context.addCompensatedStep("CANCEL_RESERVATION");
                log.info("Reservation compensation completed successfully");
            } catch (Exception e) {
                log.error("Failed to compensate reservation: {}", e.getMessage());
            }
        }
    }
    
    private ReservationDto createReservation(String userId, String journeyDate,
                                           String seatNumber, Double amount) {
        String url = String.format("%s?userId=%s&journeyDate=%s&seatNumber=%s&amount=%s",
                reservationServiceUrl, userId, journeyDate, seatNumber, amount);
        log.info("Calling reservation service: {}", url);
        return restTemplate.postForObject(url, null, ReservationDto.class);
    }
    
    private ReservationDto cancelReservation(String reservationId) {
        String url = String.format("%s/%s/cancel", reservationServiceUrl, reservationId);
        log.info("Calling reservation cancellation: {}", url);
        return restTemplate.postForObject(url, null, ReservationDto.class);
    }
    
    private PaymentDto processPayment(String reservationId, Double amount) {
        String url = String.format("%s?reservationId=%s&amount=%s",
                paymentServiceUrl, reservationId, amount);
        log.info("Calling payment service: {}", url);
        return restTemplate.postForObject(url, null, PaymentDto.class);
    }
    
    private PaymentDto refundPayment(String paymentId) {
        String url = String.format("%s/%s/refund", paymentServiceUrl, paymentId);
        log.info("Calling payment refund: {}", url);
        return restTemplate.postForObject(url, null, PaymentDto.class);
    }
    
    private NotificationDto sendConfirmation(String reservationId) {
        String url = String.format("%s/confirm?reservationId=%s",
                notificationServiceUrl, reservationId);
        log.info("Calling notification service: {}", url);
        return restTemplate.postForObject(url, null, NotificationDto.class);
    }
}

