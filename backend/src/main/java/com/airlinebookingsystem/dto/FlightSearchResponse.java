package com.airlinebookingsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing flight search results.
 * Contains detailed information about available flights matching search criteria.
 */
@Data
public class FlightSearchResponse {
    private Long id;
    private String flightNumber;
    private String airlineName;
    private String airlineCode;
    private String departureAirport;
    private String arrivalAirport;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer duration;
    private BigDecimal price;
    private Integer availableSeats;
    private String aircraft;
    private Boolean hasWifi;
    private Boolean hasMeals;
    private Boolean hasEntertainment;
}
