package com.airlinebookingsystem.repository;

import com.airlinebookingsystem.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Passenger entities in the database.
 * Provides CRUD operations and custom query methods for passengers.
 */
@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    /**
     * Retrieves all passengers associated with a specific booking.
     *
     * @param bookingId the ID of the booking to search for
     * @return a list of passengers for the specified booking
     */
    @Query("SELECT p FROM Passenger p WHERE p.booking.id = :bookingId")
    List<Passenger> findByBookingId(@Param("bookingId") Long bookingId);

    /**
     * Finds passengers by their passport number.
     *
     * @param passportNumber the passport number to search for
     * @return a list of passengers with the specified passport number
     */
    @Query("SELECT p FROM Passenger p WHERE p.passportNumber = :passportNumber")
    List<Passenger> findByPassportNumber(@Param("passportNumber") String passportNumber);

    /**
     * Retrieves all passengers booked on a specific flight.
     *
     * @param flightId the ID of the flight to search for
     * @return a list of passengers booked on the specified flight
     */
    @Query("SELECT p FROM Passenger p WHERE p.booking.flight.id = :flightId")
    List<Passenger> findByFlightId(@Param("flightId") Long flightId);
}