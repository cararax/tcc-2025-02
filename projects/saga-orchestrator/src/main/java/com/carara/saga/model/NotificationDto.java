package com.carara.saga.model;

import lombok.Data;

@Data
public class NotificationDto {
    private String notificationId;
    private String reservationId;
    private String notificationType;
    private String status;
} 