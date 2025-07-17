package com.airlinebookingsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a booking response.
 * Contains comprehensive information about a booking, including flight details,
 * passenger information, and booking status.
 */
@Data
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer numberOfPassengers;
    private BigDecimal totalAmount;
    private String status;
    private String seatClass;
    private String userEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}