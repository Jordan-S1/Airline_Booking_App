package com.airlinebookingsystem.repository;

import com.airlinebookingsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Booking entities in the database.
 * Provides CRUD operations and custom query methods for bookings.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds a booking by its unique booking reference.
     *
     * @param bookingReference the booking reference to search for
     * @return an Optional containing the booking if found, or empty if not found
     */
    Optional<Booking> findByBookingReference(String bookingReference);

    /**
     * Retrieves all bookings for a specific user, ordered by creation date descending.
     *
     * @param userId the ID of the user to find bookings for
     * @return a list of bookings for the specified user
     */
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);

    /**
     * Retrieves all bookings for a specific flight.
     *
     * @param flightId the ID of the flight to find bookings for
     * @return a list of bookings for the specified flight
     */
    @Query("SELECT b FROM Booking b WHERE b.flight.id = :flightId")
    List<Booking> findByFlightId(@Param("flightId") Long flightId);

    /**
     * Retrieves all bookings with a specific status.
     *
     * @param status the booking status to search for
     * @return a list of bookings with the specified status
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") Booking.BookingStatus status);

    /**
     * Finds all bookings created within a specified date range.
     *
     * @param startDate the start of the date range
     * @param endDate   the end of the date range
     * @return a list of bookings created within the specified date range
     */
    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Booking> findBookingsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Checks if a booking exists with the given booking reference.
     *
     * @param bookingReference the booking reference to check
     * @return true if a booking exists with the given reference, false otherwise
     */
    boolean existsByBookingReference(String bookingReference);
}
