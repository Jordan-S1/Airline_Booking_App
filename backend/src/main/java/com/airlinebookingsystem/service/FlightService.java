package com.airlinebookingsystem.service;

import com.airlinebookingsystem.dto.FlightSearchRequest;
import com.airlinebookingsystem.dto.FlightSearchResponse;
import com.airlinebookingsystem.entity.Airport;
import com.airlinebookingsystem.entity.Flight;
import com.airlinebookingsystem.repository.AirportRepository;
import com.airlinebookingsystem.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing flight operations in the airline booking system.
 * Handles flight-related business logic including searching, creating, updating, and deleting flights.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FlightService {
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;

    /**
     * Retrieves all flights from the system.
     *
     * @return a list of all flights
     */
    public List<Flight> getAllFlights() {
        log.info("Fetching all flights");
        return flightRepository.findAll();
    }

    /**
     * Retrieves a flight by its ID.
     *
     * @param id the ID of the flight to retrieve
     * @return an Optional containing the flight if found, or empty if not found
     */
    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    /**
     * Retrieves a flight by its flight number.
     *
     * @param flightNumber the flight number to search for
     * @return an Optional containing the flight if found, or empty if not found
     */
    public Optional<Flight> getFlightByNumber(String flightNumber) {
        log.info("Fetching flight with number: {}", flightNumber);
        return flightRepository.findByFlightNumber(flightNumber);
    }

    /**
     * Searches for available flights based on search criteria.
     *
     * @param request the search criteria including departure airport, arrival airport, and date
     * @return a list of flights matching the search criteria
     * @throws RuntimeException if departure or arrival airport is not found
     */
    public List<FlightSearchResponse> searchFlights(FlightSearchRequest request) {
        log.info("Searching flights from {} to {} on {}",
                request.getDepartureAirport(),
                request.getArrivalAirport(),
                request.getDepartureDate());

        Airport departure = airportRepository.findByCode(request.getDepartureAirport())
                .orElseThrow(() -> new RuntimeException("Departure airport not found"));

        Airport arrival = airportRepository.findByCode(request.getArrivalAirport())
                .orElseThrow(() -> new RuntimeException("Arrival airport not found"));

        LocalDateTime departureDateTime = request.getDepartureDate().atStartOfDay();

        List<Flight> flights = flightRepository.findAvailableFlights(
                departure, arrival, departureDateTime);

        return flights.stream()
                .filter(flight -> flight.getAvailableSeats() >= request.getPassengers())
                .map(this::mapToFlightSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new flight in the system.
     *
     * @param flight the flight to create
     * @return the created flight with assigned ID
     */
    public Flight createFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    /**
     * Updates an existing flight's details.
     *
     * @param id            the ID of the flight to update
     * @param flightDetails the new flight details
     * @return the updated flight
     * @throws RuntimeException if the flight is not found
     */
    public Flight updateFlight(Long id, Flight flightDetails) {
        log.info("Updating flight with ID: {}", id);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        flight.setDepartureTime(flightDetails.getDepartureTime());
        flight.setArrivalTime(flightDetails.getArrivalTime());
        flight.setBasePrice(flightDetails.getBasePrice());
        flight.setAvailableSeats(flightDetails.getAvailableSeats());
        flight.setStatus(flightDetails.getStatus());

        return flightRepository.save(flight);
    }

    /**
     * Deletes a flight from the system.
     *
     * @param id the ID of the flight to delete
     */
    public void deleteFlight(Long id) {
        log.info("Deleting flight with ID: {}", id);
        flightRepository.deleteById(id);
    }

    /**
     * Retrieves all upcoming flights from the current date and time.
     *
     * @return a list of upcoming flights
     */
    public List<FlightSearchResponse> getUpcomingFlights() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Fetching upcoming flights after: {}", now);
        return flightRepository.findUpcomingFlights(now).stream()
                .map(this::mapToFlightSearchResponse)
                .collect(Collectors.toList());

    }

    /**
     * Maps a Flight entity to a FlightSearchResponse DTO.
     *
     * @param flight the flight entity to map
     * @return the mapped FlightSearchResponse
     */
    private FlightSearchResponse mapToFlightSearchResponse(Flight flight) {
        FlightSearchResponse response = new FlightSearchResponse();
        response.setId(flight.getId());
        response.setFlightNumber(flight.getFlightNumber());
        response.setAirlineName(flight.getAirline().getName());
        response.setAirlineCode(flight.getAirline().getCode());
        response.setDepartureAirport(flight.getDepartureAirport().getCode());
        response.setArrivalAirport(flight.getArrivalAirport().getCode());
        response.setDepartureCity(flight.getDepartureAirport().getCity());
        response.setArrivalCity(flight.getArrivalAirport().getCity());
        response.setDepartureTime(flight.getDepartureTime());
        response.setArrivalTime(flight.getArrivalTime());
        response.setDuration(flight.getDuration());
        response.setPrice(flight.getBasePrice());
        response.setAvailableSeats(flight.getAvailableSeats());
        response.setAircraft(flight.getAircraft());

        return response;

    }


}
