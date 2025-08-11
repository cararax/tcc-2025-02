package com.carara.saga.model;

import lombok.Data;

@Data
public class TransactionContext {
    private ReservationDto reservation;
    private PaymentDto payment;
    private NotificationDto notification;
} 