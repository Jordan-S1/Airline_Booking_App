package com.airlinebookingsystem.repository;

import com.airlinebookingsystem.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Airport entities in the database.
 * Provides CRUD operations and custom query methods for airports.
 */
@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {

    /**
     * Finds an airport by its unique code.
     *
     * @param code the airport code to search for
     * @return an Optional containing the airport if found, or empty if not found
     */
    Optional<Airport> findByCode(String code);

    /**
     * Searches for airports matching a given query string in city, name, or code fields.
     * The search is case-insensitive and uses partial matching.
     *
     * @param query the search string to match against airport fields
     * @return a list of airports matching the search criteria
     */
    @Query("SELECT a FROM Airport a WHERE " +
            "LOWER(a.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.code) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Airport> findBySearchQuery(@Param("query") String query);

    /**
     * Retrieves all airports in a specific country, ordered by city name.
     *
     * @param country the country to search for airports in
     * @return a list of airports in the specified country, ordered by city name
     */
    List<Airport> findByCountryOrderByCity(String country);
}