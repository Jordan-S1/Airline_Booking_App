package com.airlinebookingsystem.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing a passenger's information for flight booking.
 * Contains all necessary personal and travel document details for a passenger.
 */
@Data
public class PassengerRequest {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String passportNumber;
    private String nationality;
    private String passengerType;
}