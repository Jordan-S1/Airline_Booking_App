package com.airlinebookingsystem.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for flight search requests.
 * Contains all necessary parameters to search for available flights.
 */
public record FlightSearchRequest (
    String departureAirport,
    String arrivalAirport,
    LocalDate departureDate,
    LocalDate returnDate,
    Integer passengers,
    String seatClass,
    Boolean directFlightsOnly
    ) {}