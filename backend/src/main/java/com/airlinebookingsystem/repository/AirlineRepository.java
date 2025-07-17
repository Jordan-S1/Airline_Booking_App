package com.airlinebookingsystem.repository;

import com.airlinebookingsystem.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Airline entities in the database.
 * Provides CRUD operations and custom query methods for airlines.
 */
@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {

    /**
     * Finds an airline by its unique code.
     *
     * @param code the airline code to search for
     * @return an Optional containing the airline if found, or empty if not found
     */
    Optional<Airline> findByCode(String code);

    /**
     * Retrieves all active airlines from the database.
     *
     * @return a list of all active airlines
     */
    @Query("SELECT a FROM Airline a WHERE a.active = true")
    List<Airline> findActiveAirlines();

    /**
     * Finds all active airlines operating in a specific country.
     *
     * @param country the country to search for airlines in
     * @return a list of active airlines in the specified country
     */
    @Query("SELECT a FROM Airline a WHERE a.country = :country AND a.active = true")
    List<Airline> findByCountry(String country);

    /**
     * Checks if an airline exists with the given code.
     *
     * @param code the airline code to check
     * @return true if an airline exists with the given code, false otherwise
     */
    boolean existsByCode(String code);
}

