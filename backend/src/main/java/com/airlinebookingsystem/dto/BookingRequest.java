package com.airlinebookingsystem.dto;

import lombok.Data;
import java.util.List;

/**
 * Data Transfer Object (DTO) representing a flight booking request.
 * Contains all necessary information to process a new flight booking.
 */

@Data
public class BookingRequest {
    private Long flightId;
    private String seatClass;
    private List<PassengerRequest> passengers;
}
