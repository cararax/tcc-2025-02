package com.carara.saga.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionContext {
    private ReservationDto reservation;
    private PaymentDto payment;
    private NotificationDto notification;
    
    // Informações sobre os steps executados
    private List<String> completedSteps = new ArrayList<>();
    private String failedStep;
    private String failureReason;
    private boolean isSuccess = false;
    private List<String> compensatedSteps = new ArrayList<>();
    
    public void addCompletedStep(String step) {
        this.completedSteps.add(step);
    }
    
    public void setFailure(String step, String reason) {
        this.failedStep = step;
        this.failureReason = reason;
        this.isSuccess = false;
    }
    
    public void addCompensatedStep(String step) {
        this.compensatedSteps.add(step);
    }
    
    public void markAsSuccess() {
        this.isSuccess = true;
    }
} 