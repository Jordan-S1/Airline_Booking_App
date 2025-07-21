package com.airlinebookingsystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a passenger response.
 * Contains comprehensive information about a passenger, including their personal details,
 * booking information, and seat assignment.
 */
public record PassengerResponse (
     Long id,
     String firstName,
     String lastName,
     LocalDate dateOfBirth,
     String gender,
     String passportNumber,
     String nationality,
     String seatNumber,
     String passengerType,
     Long bookingId,
     String bookingReference,
     String flightNumber,
     LocalDateTime createdAt,
     LocalDateTime updatedAt
){}