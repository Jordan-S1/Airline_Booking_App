package com.airlinebookingsystem.repository;

import com.airlinebookingsystem.entity.Flight;
import com.airlinebookingsystem.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Flight entities in the database.
 * Provides CRUD operations and custom query methods for flights.
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    /**
     * Finds a flight by its unique flight number.
     *
     * @param flightNumber the flight number to search for
     * @return an Optional containing the flight if found, or empty if not found
     */
    Optional<Flight> findByFlightNumber(String flightNumber);

    /**
     * Finds available flights matching the specified criteria.
     *
     * @param departure     the departure airport
     * @param arrival       the arrival airport
     * @param departureDate the desired departure date and time
     * @return a list of available flights matching the criteria
     */
    @Query("SELECT f FROM Flight f WHERE " +
            "f.departureAirport = :departure AND " +
            "f.arrivalAirport = :arrival AND " +
            "DATE(f.departureTime) = DATE(:departureDate) AND " +
            "f.active = true AND " +
            "f.availableSeats > 0")
    List<Flight> findAvailableFlights(
            @Param("departure") Airport departure,
            @Param("arrival") Airport arrival,
            @Param("departureDate") LocalDateTime departureDate
    );

    /**
     * Searches for flights based on departure and arrival airports and date range.
     *
     * @param departureCode the departure airport code
     * @param arrivalCode   the arrival airport code
     * @param startDate     the start of the date range
     * @param endDate       the end of the date range
     * @return a list of flights matching the search criteria
     */
    @Query("SELECT f FROM Flight f WHERE " +
            "f.departureAirport.code = :departureCode AND " +
            "f.arrivalAirport.code = :arrivalCode AND " +
            "f.departureTime BETWEEN :startDate AND :endDate AND " +
            "f.active = true")
    List<Flight> searchFlights(
            @Param("departureCode") String departureCode,
            @Param("arrivalCode") String arrivalCode,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Retrieves all flights operated by a specific airline using airline code.
     *
     * @param airlineCode the code of the airline
     * @return a list of flights operated by the specified airline
     */
    @Query("SELECT f FROM Flight f WHERE f.airline.code = :airlineCode AND f.active = true")
    List<Flight> findByAirlineCode(@Param("airlineCode") String airlineCode);

    /**
     * Retrieves all upcoming active flights after the specified datetime.
     *
     * @param now the current datetime to compare against
     * @return a list of upcoming flights
     */
    @Query("SELECT f FROM Flight f WHERE f.departureTime > :now AND f.active = true")
    List<Flight> findUpcomingFlights(@Param("now") LocalDateTime now);
}
