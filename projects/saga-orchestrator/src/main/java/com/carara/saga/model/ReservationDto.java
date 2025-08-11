package com.carara.saga.model;

import lombok.Data;

@Data
public class ReservationDto {
    private String reservationId;
    private String userId;
    private String journeyDate;
    private String status;
    private String seatNumber;
    private Double amount;
} 