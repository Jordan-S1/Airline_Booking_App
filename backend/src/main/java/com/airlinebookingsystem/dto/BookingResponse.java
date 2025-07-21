package com.airlinebookingsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a booking response.
 * Contains comprehensive information about a booking, including flight details,
 * passenger information, and booking status.
 */
public record BookingResponse(
    Long id,
    String bookingReference,
    String flightNumber,
    String departureAirport,
    String arrivalAirport,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    Integer numberOfPassengers,
    BigDecimal totalAmount,
    String status,
    String seatClass,
    String userEmail,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
){}