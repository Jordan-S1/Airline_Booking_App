package com.airlinebookingsystem.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a passenger response.
 * Contains comprehensive information about a passenger, including their personal details,
 * booking information, and seat assignment.
 */
@Data
public class PassengerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String passportNumber;
    private String nationality;
    private String seatNumber;
    private String passengerType;
    private Long bookingId;
    private String bookingReference;
    private String flightNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}