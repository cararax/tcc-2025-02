package com.carara.reservation.service;

import com.carara.reservation.model.Reservation;
import com.carara.reservation.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(String userId, String journeyDate,
                                       String seatNumber, Double amount) {
        log.info("Starting reservation creation for user {} - Journey: {}, Seat: {}, Amount: {}", 
                userId, journeyDate, seatNumber, amount);
        
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setJourneyDate(journeyDate);
        reservation.setSeatNumber(seatNumber);
        reservation.setAmount(amount);
        reservation.setStatus("CREATED");
        
        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", savedReservation.getReservationId());
        return savedReservation;
    }

    public Reservation cancelReservation(String reservationId) {
        log.info("Starting cancellation process for reservation: {}", reservationId);
        
        return reservationRepository.findById(reservationId).map(reservation -> {
            log.info("Found reservation to cancel: {}", reservation);
            reservation.setStatus("CANCELED");
            Reservation canceledReservation = reservationRepository.save(reservation);
            log.info("Reservation {} canceled successfully", reservationId);
            return canceledReservation;
        }).orElseGet(() -> {
            log.info("Reservation {} not found for cancellation", reservationId);
            return null;
        });
    }
} 