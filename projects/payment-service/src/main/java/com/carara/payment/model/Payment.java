package com.carara.payment.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Data
public class Payment {
    @Id
    private String paymentId = UUID.randomUUID().toString();
    private String reservationId;
    private Double amount;
    private String status;
} 