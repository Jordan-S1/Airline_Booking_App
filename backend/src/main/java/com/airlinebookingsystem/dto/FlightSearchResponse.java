package com.airlinebookingsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing flight search results.
 * Contains detailed information about available flights matching search criteria.
 */
public record FlightSearchResponse (
    Long id,
    String flightNumber,
    String airlineName,
    String airlineCode,
    String departureAirport,
    String arrivalAirport,
    String departureCity,
    String arrivalCity,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    Integer duration,
    BigDecimal price,
    Integer availableSeats,
    String aircraft
    ){}
