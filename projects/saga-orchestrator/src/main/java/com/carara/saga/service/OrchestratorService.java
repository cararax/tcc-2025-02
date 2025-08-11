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
        log.info("Starting transaction execution for user {} - Journey: {}, Seat: {}, Amount: {}", 
                userId, journeyDate, seatNumber, amount);
        
        TransactionContext context = new TransactionContext();
        
        try {
            log.info("Step 1: Creating reservation");
            ReservationDto reservation = createReservation(userId, journeyDate, seatNumber, amount);
            context.setReservation(reservation);
            log.info("Reservation created successfully - ID: {}", reservation.getReservationId());
            
            log.info("Step 2: Processing payment");
            PaymentDto payment = processPayment(reservation.getReservationId(), amount);
            context.setPayment(payment);
            log.info("Payment processed successfully - ID: {}", payment.getPaymentId());
            
            log.info("Step 3: Sending confirmation");
            NotificationDto notification = sendConfirmation(context.getReservation().getReservationId());
            context.setNotification(notification);
            log.info("Confirmation sent successfully - ID: {}", notification.getNotificationId());
            
            log.info("Transaction completed successfully for user {}", userId);
            return context;
        } catch (Exception e) {
            log.error("Transaction failed for user {} - Error: {}", userId, e.getMessage());
            log.info("Starting compensation process");
            
            // Execute compensation
            if (context.getPayment() != null) {
                log.info("Compensating payment - ID: {}", context.getPayment().getPaymentId());
                refundPayment(context.getPayment().getPaymentId());
            }
            if (context.getReservation() != null) {
                log.info("Compensating reservation - ID: {}", context.getReservation().getReservationId());
                cancelReservation(context.getReservation().getReservationId());
            }
            
            log.error("Compensation completed for failed transaction", e.getMessage());
            throw new RuntimeException("Transaction failed", e);
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

