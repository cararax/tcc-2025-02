package com.carara.reservation.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Data
public class Reservation {
    @Id
    private String reservationId = UUID.randomUUID().toString();
    private String userId;
    private String journeyDate;
    private String status;
    private String seatNumber;
    private Double amount;
} 