package com.carara.payment.service;

import com.carara.payment.model.Payment;
import com.carara.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final Random random = new Random();

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(String reservationId, Double amount) {
        log.info("Starting payment processing for reservation {} with amount {}", reservationId, amount);

        if (random.nextDouble() > 0.8) {
            log.error("Payment processing failed for reservation {} - Random failure triggered", reservationId);
            throw new RuntimeException("Payment processing failed");
        }

        Payment payment = new Payment();
        payment.setReservationId(reservationId);
        payment.setAmount(amount);
        payment.setStatus("APPROVED");

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully - Payment ID: {}, Reservation ID: {}, Amount: {}", savedPayment.getPaymentId(), reservationId, amount);
        return savedPayment;
    }

    public Payment refundPayment(String paymentId) {
        log.info("Starting refund process for payment: {}", paymentId);

        return paymentRepository.findById(paymentId).map(payment -> {
            log.info("Found payment to refund: {}", payment);
            payment.setStatus("REFUNDED");
            Payment refundedPayment = paymentRepository.save(payment);
            log.info("Payment {} refunded successfully", paymentId);
            return refundedPayment;
        }).orElseGet(() -> {
            log.info("Payment {} not found for refund", paymentId);
            return null;
        });
    }
} 