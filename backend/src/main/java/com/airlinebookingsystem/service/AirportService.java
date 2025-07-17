package com.airlinebookingsystem.service;

import com.airlinebookingsystem.entity.Airport;
import com.airlinebookingsystem.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing airport-related operations in the airline booking system.
 * Provides methods for retrieving, searching, creating, and deleting airport records.
 * All operations are transactional and interact with the AirportRepository.
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AirportService {
    private final AirportRepository airportRepository;

    /**
     * Retrieves all airports from the database.
     *
     * @return a list of all airports in the system
     */
    public List<Airport> getAllAirports() {
        log.info("Fetching all airports");
        return airportRepository.findAll();
    }

    /**
     * Finds an airport by its code.
     *
     * @param code the airport code to search for (will be converted to uppercase)
     * @return an Optional containing the airport if found, or empty if not found
     */
    public Optional<Airport> getAirportByCode(String code) {
        return airportRepository.findByCode(code.toUpperCase());
    }

    /**
     * Searches for airports based on a query string.
     * If the query is null or empty, it returns all airports.
     *
     * @param query the search string to match against airport fields
     * @return a list of airports matching the search criteria
     */
    public List<Airport> searchAirports(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllAirports();
        }
        return airportRepository.findBySearchQuery(query.trim());
    }

    /**
     * Retrieves all airports in a specific country, ordered by city name.
     *
     * @param country the country to search for airports in
     * @return a list of airports in the specified country, ordered by city name
     */
    public List<Airport> getAirportsByCountry(String country) {
        log.info("Fetching airports for country: {}", country);
        return airportRepository.findByCountryOrderByCity(country);
    }

    /**
     * Saves or updates an airport in the database.
     * The airport code will be converted to uppercase before saving.
     *
     * @param airport the airport entity to save
     * @return the saved airport entity with updated information
     */
    public Airport saveAirport(Airport airport) {
        airport.setCode(airport.getCode().toUpperCase());
        return airportRepository.save(airport);
    }

    /**
     * Deletes an airport from the database by its ID.
     *
     * @param id the unique identifier of the airport to delete
     */
    public void deleteAirport(Long id) {
        airportRepository.deleteById(id);
    }
}
