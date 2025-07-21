package com.airlinebookingsystem.dto;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing a flight booking request.
 * Contains all necessary information to process a new flight booking.
 */

public record BookingRequest (
    Long flightId,
    String seatClass,
    List<PassengerRequest> passengers
){}
