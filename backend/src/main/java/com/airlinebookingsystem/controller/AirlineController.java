package com.airlinebookingsystem.controller;

import com.airlinebookingsystem.entity.Airline;
import com.airlinebookingsystem.service.AirlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing airline operations in the airline booking system.
 * Provides endpoints for CRUD operations and airline-specific queries.
 */
@RestController
@RequestMapping("api/v1/airlines")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AirlineController {

    private final AirlineService airlineService;

    /**
     * Retrieves all airlines from the system.
     *
     * @return ResponseEntity containing a list of all airlines
     */
    @GetMapping
    public ResponseEntity<List<Airline>> getAllAirlines() {
        log.info("GET request received for all airlines");
        try {
            List<Airline> airlines = airlineService.getAllAirlines();
            return ResponseEntity.ok(airlines);
        } catch (Exception e) {
            log.error("Error fetching all airlines: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all active airlines from the system.
     *
     * @return ResponseEntity containing a list of all active airlines
     */
    @GetMapping("/active")
    public ResponseEntity<List<Airline>> getAllActiveAirlines() {
        log.info("GET request received for all active airlines");
        try {
            List<Airline> activeAirlines = airlineService.getAllActiveAirlines();
            return ResponseEntity.ok(activeAirlines);
        } catch (Exception e) {
            log.error("Error fetching active airlines: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves an airline by its ID.
     *
     * @param id the ID of the airline to retrieve
     * @return ResponseEntity containing the airline if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Airline> getAirlineById(@PathVariable Long id) {
        log.info("GET request received for airline with ID: {}", id);
        try {
            Optional<Airline> airline = airlineService.getAirlineById(id);
            return airline.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching airline with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves an airline by its code.
     *
     * @param code the airline code to search for
     * @return ResponseEntity containing the airline if found, or 404 if not found
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Airline> getAirlineByCode(@PathVariable String code) {
        log.info("GET request received for airline with code: {}", code);
        try {
            Optional<Airline> airline = airlineService.getAirlineByCode(code);
            return airline.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching airline with code {}: {}", code, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves airlines by country.
     *
     * @param country the country to search for airlines in
     * @return ResponseEntity containing a list of airlines in the specified country
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<List<Airline>> getAirlinesByCountry(@PathVariable String country) {
        log.info("GET request received for airlines in country: {}", country);
        try {
            List<Airline> airlines = airlineService.getAirlinesByCountry(country);
            return ResponseEntity.ok(airlines);
        } catch (Exception e) {
            log.error("Error fetching airlines in country {}: {}", country, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new airline in the system.
     *
     * @param airline the airline to create
     * @return ResponseEntity containing the created airline with status 201
     */
    @PostMapping
    public ResponseEntity<Airline> createAirline(@Valid @RequestBody Airline airline) {
        log.info("POST request received to create airline with code: {}", airline.getCode());
        try {
            Airline createdAirline = airlineService.createAirline(airline);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAirline);
        } catch (RuntimeException e) {
            log.error("Error creating airline: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Unexpected error creating airline: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing airline's details.
     *
     * @param id            the ID of the airline to update
     * @param airlineDetails the new airline details
     * @return ResponseEntity containing the updated airline
     */
    @PutMapping("/{id}")
    public ResponseEntity<Airline> updateAirline(@PathVariable Long id,
                                                 @Valid @RequestBody Airline airlineDetails) {
        log.info("PUT request received to update airline with ID: {}", id);
        try {
            Airline updatedAirline = airlineService.updateAirline(id, airlineDetails);
            return ResponseEntity.ok(updatedAirline);
        } catch (RuntimeException e) {
            log.error("Error updating airline with ID {}: {}", id, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error updating airline with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deactivates an airline (soft delete).
     *
     * @param id the ID of the airline to deactivate
     * @return ResponseEntity with status 204 if successful
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateAirline(@PathVariable Long id) {
        log.info("PATCH request received to deactivate airline with ID: {}", id);
        try {
            airlineService.deactivateAirline(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deactivating airline with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error deactivating airline with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reactivates a previously deactivated airline.
     *
     * @param id the ID of the airline to reactivate
     * @return ResponseEntity with status 204 if successful
     */
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateAirline(@PathVariable Long id) {
        log.info("PATCH request received to reactivate airline with ID: {}", id);
        try {
            airlineService.reactivateAirline(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error reactivating airline with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error reactivating airline with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Permanently deletes an airline from the system.
     * Use with caution - this will affect flight records.
     *
     * @param id the ID of the airline to delete
     * @return ResponseEntity with status 204 if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAirline(@PathVariable Long id) {
        log.info("DELETE request received for airline with ID: {}", id);
        try {
            airlineService.deleteAirline(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting airline with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
