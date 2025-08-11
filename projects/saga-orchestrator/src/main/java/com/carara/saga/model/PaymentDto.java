package com.carara.saga.model;

import lombok.Data;

@Data
public class PaymentDto {
    private String paymentId;
    private String reservationId;
    private Double amount;
    private String status;
} 