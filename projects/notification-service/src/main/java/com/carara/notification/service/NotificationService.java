package com.carara.notification.service;

import com.carara.notification.model.Notification;
import com.carara.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Random;

@Slf4j
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final Random random = new Random();

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification sendConfirmation(String reservationId) {
        log.info("Starting confirmation notification process for reservation: {}", reservationId);
        
        if (random.nextDouble() > 0.9) {
            log.error("Confirmation notification failed for reservation {} - Random failure triggered", reservationId);
            throw new RuntimeException("Confirmation sending failed");
        }
        
        Notification notification = new Notification();
        notification.setReservationId(reservationId);
        notification.setNotificationType("CONFIRMATION");
        notification.setStatus("SENT");
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Confirmation notification sent successfully - Notification ID: {}, Reservation ID: {}", 
                savedNotification.getNotificationId(), reservationId);
        return savedNotification;
    }

    public Notification sendCancellation(String notificationId) {
        log.info("Starting cancellation notification process for notification: {}", notificationId);
        
        return notificationRepository.findById(notificationId).map(notification -> {
            log.info("Found original notification for cancellation: {}", notification);
            Notification cancellationNotice = new Notification();
            cancellationNotice.setReservationId(notification.getReservationId());
            cancellationNotice.setNotificationType("CANCELLATION");
            cancellationNotice.setStatus("SENT");
            
            Notification savedCancellation = notificationRepository.save(cancellationNotice);
            log.info("Cancellation notification sent successfully - Notification ID: {}, Original Notification ID: {}", 
                    savedCancellation.getNotificationId(), notificationId);
            return savedCancellation;
        }).orElseGet(() -> {
            log.info("Original notification {} not found, creating standalone cancellation", notificationId);
            Notification cancellation = new Notification();
            cancellation.setNotificationType("CANCELLATION");
            cancellation.setStatus("SENT");
            
            Notification savedCancellation = notificationRepository.save(cancellation);
            log.info("Standalone cancellation notification created - Notification ID: {}", 
                    savedCancellation.getNotificationId());
            return savedCancellation;
        });
    }
} 