package com.airlinebookingsystem.dto;

import com.airlinebookingsystem.entity.Payment;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object representing a payment request for flight booking.
 * Contains all necessary information to process a payment.
 */
public record PaymentRequest (
        Long bookingId,
        BigDecimal amount,
        Payment.PaymentMethod paymentMethod,
        Map<String, String> paymentDetails
){}

