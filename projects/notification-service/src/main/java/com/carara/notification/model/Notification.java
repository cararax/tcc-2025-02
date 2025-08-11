package com.carara.notification.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Data
public class Notification {
    @Id
    private String notificationId = UUID.randomUUID().toString();
    private String reservationId;
    private String notificationType;
    private String status;
} 