package com.airlinebookingsystem.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing a passenger's information for flight booking.
 * Contains all necessary personal and travel document details for a passenger.
 */
public record PassengerRequest (
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String passportNumber,
        String nationality,
        String passengerType
){}