package com.airlinebookingsystem.repository;

import com.airlinebookingsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Payment entities in the database.
 * Provides CRUD operations and custom query methods for payments.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds a payment by its unique transaction ID.
     *
     * @param transactionId the transaction ID to search for
     * @return an Optional containing the payment if found, or empty if not found
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Retrieves a payment associated with a specific booking ID.
     *
     * @param bookingId the ID of the booking to find payment for
     * @return an Optional containing the payment if found, or empty if not found
     */
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId")
    Optional<Payment> findByBookingId(@Param("bookingId") Long bookingId);

    /**
     * Finds all payments with a specific payment status.
     *
     * @param status the payment status to filter by
     * @return a list of payments with the specified status
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findByStatus(@Param("status") Payment.PaymentStatus status);

    /**
     * Retrieves all payments created within a specific date range.
     *
     * @param startDate the start date and time of the range
     * @param endDate   the end date and time of the range
     * @return a list of payments created within the specified date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Checks if a payment with the given transaction ID exists.
     *
     * @param transactionId the transaction ID to check
     * @return true if a payment with the transaction ID exists, false otherwise
     */
    boolean existsByTransactionId(String transactionId);
}
