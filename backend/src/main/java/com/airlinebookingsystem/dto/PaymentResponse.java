package com.airlinebookingsystem.dto;

import com.airlinebookingsystem.entity.Payment;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a payment response.
 * Contains payment processing result and transaction details.
 */
@Data
public class PaymentResponse {
    private Long id;
    private String transactionId;
    private Long bookingId;
    private String bookingReference;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus status;
    private String paymentGatewayResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}