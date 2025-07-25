package com.airlinebookingsystem.controller;

import com.airlinebookingsystem.dto.FlightSearchRequest;
import com.airlinebookingsystem.dto.FlightSearchResponse;
import com.airlinebookingsystem.dto.FlightSearchResult;
import com.airlinebookingsystem.entity.Flight;
import com.airlinebookingsystem.service.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing flight operations in the airline booking system.
 * Provides endpoints for flight search, CRUD operations, and flight-specific queries.
 */
@RestController
@RequestMapping("api/v1/flights")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FlightController {

    private final FlightService flightService;

    /**
     * Retrieves all flights from the system.
     *
     * @return ResponseEntity containing a list of all flights
     */
    @GetMapping
    public ResponseEntity<List<Flight>> getAllFlights() {
        log.info("GET request received for all flights");
        try {
            List<Flight> flights = flightService.getAllFlights();
            return ResponseEntity.ok(flights);
        } catch (Exception e) {
            log.error("Error fetching all flights: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a flight by its ID.
     *
     * @param id the ID of the flight to retrieve
     * @return ResponseEntity containing the flight if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlightById(@PathVariable Long id) {
        log.info("GET request received for flight with ID: {}", id);
        try {
            Optional<Flight> flight = flightService.getFlightById(id);
            return flight.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching flight with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a flight by its flight number.
     *
     * @param flightNumber the flight number to search for (must not be blank)
     * @return ResponseEntity containing the flight if found, or 404 if not found
     */
    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<Flight> getFlightByNumber(@PathVariable @NotBlank String flightNumber) {
        log.info("GET request received for flight with number: {}", flightNumber);
        try {
            Optional<Flight> flight = flightService.getFlightByNumber(flightNumber);
            return flight.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching flight with number {}: {}", flightNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Searches for available flights based on search criteria.
     * Supports both one-way and round-trip searches.
     * Request body should contain:
     * - departureAirport: Airport code (e.g., "LAX")
     * - arrivalAirport: Airport code (e.g., "JFK")
     * - departureDate: Departure date in YYYY-MM-DD format
     * - returnDate: Return date (optional, for round-trip)
     * - passengers: Number of passengers (optional, defaults to 1)
     * - seatClass: Seat class preference (optional, defaults to "ECONOMY")
     * - directFlightsOnly: Filter for direct flights only (optional, defaults to false)
     *
     * @param request the FlightSearchRequest DTO with search criteria
     * @return ResponseEntity containing FlightSearchResult with outbound and optional return flights
     */
    @PostMapping("/search")
    public ResponseEntity<FlightSearchResult> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        log.info("POST request received for flight search from {} to {} on {} (passengers: {}, class: {}, direct only: {})",
                request.departureAirport(),
                request.arrivalAirport(),
                request.departureDate(),
                request.passengers(),
                request.seatClass(),
                request.directFlightsOnly());
        try {
            FlightSearchResult searchResult = flightService.searchFlights(request);
            log.info("Flight search completed: {} outbound flights, {} return flights",
                    searchResult.outboundFlights().size(),
                    searchResult.returnFlights() != null ? searchResult.returnFlights().size() : 0);
            return ResponseEntity.ok(searchResult);
        } catch (RuntimeException e) {
            log.error("Error searching flights: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error searching flights: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all upcoming flights from the current date and time.
     *
     * @return ResponseEntity containing a list of upcoming flights
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<FlightSearchResponse>> getUpcomingFlights() {
        log.info("GET request received for upcoming flights");
        try {
            List<FlightSearchResponse> upcomingFlights = flightService.getUpcomingFlights();
            return ResponseEntity.ok(upcomingFlights);
        } catch (Exception e) {
            log.error("Error fetching upcoming flights: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all flights operated by a specific airline using airline code.
     *
     * @param airlineCode the code of the airline (e.g., "AA", "DL", "UA") - must not be blank
     * @return ResponseEntity containing a list of FlightSearchResponse DTOs for the specified airline
     */
    @GetMapping("/airline/{airlineCode}")
    public ResponseEntity<List<FlightSearchResponse>> getFlightsByAirlineCode(@PathVariable @NotBlank String airlineCode) {
        log.info("GET request received for flights by airline code: {}", airlineCode);
        try {
            List<FlightSearchResponse> flights = flightService.getFlightsByAirlineCode(airlineCode);
            return ResponseEntity.ok(flights);
        } catch (RuntimeException e) {
            log.error("Error fetching flights for airline {}: {}", airlineCode, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error fetching flights for airline {}: {}", airlineCode, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new flight in the system.
     *
     * @param flight the flight to create
     * @return ResponseEntity containing the created flight with status 201
     */
    @PostMapping
    public ResponseEntity<Flight> createFlight(@Valid @RequestBody Flight flight) {
        log.info("POST request received to create flight with number: {}", flight.getFlightNumber());
        try {
            Flight createdFlight = flightService.createFlight(flight);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFlight);
        } catch (RuntimeException e) {
            log.error("Error creating flight: {}", e.getMessage());
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error creating flight: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing flight's details.
     *
     * @param id            the ID of the flight to update
     * @param flightDetails the new flight details
     * @return ResponseEntity containing the updated flight
     */
    @PutMapping("/{id}")
    public ResponseEntity<Flight> updateFlight(@PathVariable Long id,
                                               @Valid @RequestBody Flight flightDetails) {
        log.info("PUT request received to update flight with ID: {}", id);
        try {
            Flight updatedFlight = flightService.updateFlight(id, flightDetails);
            return ResponseEntity.ok(updatedFlight);
        } catch (RuntimeException e) {
            log.error("Error updating flight with ID {}: {}", id, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error updating flight with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a flight from the system.
     *
     * @param id the ID of the flight to delete
     * @return ResponseEntity with status 204 if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        log.info("DELETE request received for flight with ID: {}", id);
        try {
            flightService.deleteFlight(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting flight with ID {}: {}", id, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error deleting flight with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
