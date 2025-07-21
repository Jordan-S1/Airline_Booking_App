package com.airlinebookingsystem.dto;

import com.airlinebookingsystem.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a payment response.
 * Contains payment processing result and transaction details.
 */
public record PaymentResponse (
     Long id,
     String transactionId,
     Long bookingId,
     String bookingReference,
     BigDecimal amount,
     Payment.PaymentMethod paymentMethod,
     Payment.PaymentStatus status,
     String paymentGatewayResponse,
     LocalDateTime createdAt,
     LocalDateTime updatedAt
){}