package com.airlinebookingsystem.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for flight search requests.
 * Contains all necessary parameters to search for available flights.
 */
@Data
public class FlightSearchRequest {
    private String departureAirport;
    private String arrivalAirport;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer passengers = 1;
    private String seatClass = "ECONOMY";
    private Boolean directFlightsOnly = false;
}
