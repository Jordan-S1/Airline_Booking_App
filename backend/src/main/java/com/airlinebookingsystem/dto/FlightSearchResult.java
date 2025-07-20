package com.airlinebookingsystem.dto;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing flight search results.
 * Supports both one-way and round-trip flight searches.
 */
public record FlightSearchResult(
        List<FlightSearchResponse> outboundFlights,
        List<FlightSearchResponse> returnFlights, // null for one-way trips
        boolean isRoundTrip
) {
}
