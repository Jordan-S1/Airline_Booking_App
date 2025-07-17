package com.airlinebookingsystem.dto;

import com.airlinebookingsystem.entity.Payment;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object representing a payment request for flight booking.
 * Contains all necessary information to process a payment.
 */
@Data
public class PaymentRequest {
    private Long bookingId;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private Map<String, String> paymentDetails;
}

