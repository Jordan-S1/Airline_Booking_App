package com.airlinebookingsystem.service;

import com.airlinebookingsystem.dto.BookingRequest;
import com.airlinebookingsystem.dto.BookingResponse;
import com.airlinebookingsystem.dto.PassengerRequest;
import com.airlinebookingsystem.dto.PassengerResponse;
import com.airlinebookingsystem.entity.*;
import com.airlinebookingsystem.repository.BookingRepository;
import com.airlinebookingsystem.repository.FlightRepository;
import com.airlinebookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final PassengerService passengerService;

    /**
     * Creates a new booking based on the provided booking request.
     *
     * @param request the booking request containing flight and passenger details
     * @param userId  the ID of the user making the booking
     * @return BookingResponse containing the created booking details
     * @throws RuntimeException if flight or user is not found, or if insufficient seats are available
     */
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        log.info("Creating booking for user {} and flight {}", userId, request.flightId());

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate flight
        Flight flight = flightRepository.findById(request.flightId())
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        // Check seat availability based on seat class
        validateSeatAvailability(flight, request.seatClass(), request.passengers().size());

        // Calculate total amount based on seat class
        BigDecimal totalAmount = calculateTotalAmount(flight, request.seatClass(), request.passengers().size());

        // Create a booking entity
        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .user(user)
                .flight(flight)
                .numberOfPassengers(request.passengers().size())
                .totalAmount(totalAmount)
                .status(Booking.BookingStatus.PENDING)
                .seatClass(Booking.SeatClass.valueOf(request.seatClass()))
                .build();

        // Save booking first to get ID
        booking = bookingRepository.save(booking);

        try {
            // Create passengers using PassengerService
            List<PassengerResponse> createdPassengers = passengerService.createPassengers(request.passengers(), booking.getId());
            log.info("Created {} passengers for booking {}", createdPassengers.size(), booking.getBookingReference());

            // Update flight availability based on seat class
            updateFlightSeatAvailability(flight, request.seatClass(), request.passengers().size(), false);
            flightRepository.save(flight);

            log.info("Booking created successfully with reference: {}", booking.getBookingReference());
            return mapToBookingResponse(booking);
        } catch (Exception e) {
            // Rollback booking creation if passenger creation fails
            log.error("Error creating passengers for booking {}, rolling back", booking.getBookingReference(), e);
            bookingRepository.delete(booking);
            throw new RuntimeException("Failed to create booking: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a booking by its reference number.
     *
     * @param bookingReference the booking reference to search for
     * @return BookingResponse containing the booking details
     * @throws RuntimeException if a booking is not found
     */
    public BookingResponse getBookingByReference(String bookingReference) {
        log.info("Retrieving booking with reference: {}", bookingReference);
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToBookingResponse(booking);
    }

    /**
     * Retrieves all bookings for a specific user.
     *
     * @param userId the ID of the user
     * @return List of BookingResponse objects
     */
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        log.info("Retrieving bookings for user: {}", userId);
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Confirms a booking by updating its status to CONFIRMED.
     *
     * @param bookingReference the booking reference to confirm
     * @return BookingResponse with updated status
     * @throws RuntimeException if a booking is not found
     */
    public BookingResponse confirmBooking(String bookingReference) {
        log.info("Confirming booking with reference: {}", bookingReference);
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            log.warn("Booking {} is already confirmed", bookingReference);
            return mapToBookingResponse(booking);
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot confirm a cancelled booking");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        log.info("Booking {} confirmed successfully", bookingReference);
        return mapToBookingResponse(booking);
    }

    /**
     * Cancels a booking and restores flight seat availability.
     *
     * @param bookingReference the booking reference to cancel
     * @return BookingResponse with updated status
     * @throws RuntimeException if a booking is not found or already canceled
     */
    public BookingResponse cancelBooking(String bookingReference) {
        log.info("Cancelling booking with reference: {}", bookingReference);
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        // Restore flight seat availability
        Flight flight = booking.getFlight();
        updateFlightSeatAvailability(flight, booking.getSeatClass().name(), booking.getNumberOfPassengers(), true);
        flightRepository.save(flight);

        log.info("Booking {} cancelled successfully", bookingReference);
        return mapToBookingResponse(booking);
    }

    /**
     * Retrieves all bookings with a specific status.
     *
     * @param status the booking status to filter by
     * @return List of BookingResponse objects
     */
    public List<BookingResponse> getBookingsByStatus(String status) {
        log.info("Retrieving bookings with status: {}", status);
        Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
        List<Booking> bookings = bookingRepository.findByStatus(bookingStatus);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves passengers for a specific booking.
     *
     * @param bookingReference the booking reference
     * @return List of PassengerResponse objects
     */
    public List<PassengerResponse> getBookingPassengers(String bookingReference) {
        log.info("Retrieving passengers for booking: {}", bookingReference);
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return passengerService.getPassengersByBookingId(booking.getId());
    }

    /**
     * Updates an existing booking (limited updates allowed).
     *
     * @param bookingReference the booking reference to update
     * @param request new booking details
     * @return updated BookingResponse
     */
    public BookingResponse updateBooking(String bookingReference, BookingRequest request) {
        log.info("Updating booking with reference: {}", bookingReference);

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found with reference: " + bookingReference));

        // Only allow updates for pending bookings
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Can only update pending bookings");
        }

        // Update seat class if changed
        if (!booking.getSeatClass().name().equals(request.seatClass().toUpperCase())) {
            // Restore old seat availability and check new seat availability
            Flight flight = booking.getFlight();
            updateFlightSeatAvailability(flight, booking.getSeatClass().name(), booking.getNumberOfPassengers(), true);
            validateSeatAvailability(flight, request.seatClass(), request.passengers().size());
            updateFlightSeatAvailability(flight, request.seatClass(), request.passengers().size(), false);

            booking.setSeatClass(Booking.SeatClass.valueOf(request.seatClass().toUpperCase()));
            booking.setTotalAmount(calculateTotalAmount(flight, request.seatClass(), request.passengers().size()));

            flightRepository.save(flight);
        }

        booking = bookingRepository.save(booking);
        log.info("Booking {} updated successfully", bookingReference);
        return mapToBookingResponse(booking);
    }

    /**
     * Validates seat availability for a specific seat class.
     */
    private void validateSeatAvailability(Flight flight, String seatClass, int requiredSeats) {
        int availableSeats = switch (seatClass.toUpperCase()) {
            case "ECONOMY" -> flight.getEconomySeats() != null ? flight.getEconomySeats() : 0;
            case "BUSINESS" -> flight.getBusinessSeats() != null ? flight.getBusinessSeats() : 0;
            case "FIRST" -> flight.getFirstClassSeats() != null ? flight.getFirstClassSeats() : 0;
            default -> flight.getAvailableSeats() != null ? flight.getAvailableSeats() : 0;
        };

        if (availableSeats < requiredSeats) {
            throw new RuntimeException(String.format("Insufficient %s class seats available. Required: %d, Available: %d",
                    seatClass.toLowerCase(), requiredSeats, availableSeats));
        }
    }

    /**
     * Updates flight seat availability based on seat class.
     */
    private void updateFlightSeatAvailability(Flight flight, String seatClass, int seatCount, boolean restore) {
        int change = restore ? seatCount : -seatCount;

        switch (seatClass.toUpperCase()) {
            case "ECONOMY" -> {
                int newCount = (flight.getEconomySeats() != null ? flight.getEconomySeats() : 0) + change;
                flight.setEconomySeats(Math.max(0, newCount));
            }
            case "BUSINESS" -> {
                int newCount = (flight.getBusinessSeats() != null ? flight.getBusinessSeats() : 0) + change;
                flight.setBusinessSeats(Math.max(0, newCount));
            }
            case "FIRST" -> {
                int newCount = (flight.getFirstClassSeats() != null ? flight.getFirstClassSeats() : 0) + change;
                flight.setFirstClassSeats(Math.max(0, newCount));
            }
            default -> {
                int newCount = (flight.getAvailableSeats() != null ? flight.getAvailableSeats() : 0) + change;
                flight.setAvailableSeats(Math.max(0, newCount));
            }
        }
    }

    /**
     * Generates a unique booking reference.
     *
     * @return a unique booking reference string
     */
    private String generateBookingReference() {
        String reference;
        do {
            reference = "BK" + System.currentTimeMillis() + 
                       String.format("%04d", new Random().nextInt(10000));
        } while (bookingRepository.existsByBookingReference(reference));
        return reference;
    }

    /**
     * Calculates the total amount for a booking based on seat class and number of passengers.
     *
     * @param flight           the flight being booked
     * @param seatClass        the seat class requested
     * @param numberOfPassengers the number of passengers
     * @return the total amount for the booking
     */
    private BigDecimal calculateTotalAmount(Flight flight, String seatClass, int numberOfPassengers) {
        BigDecimal pricePerSeat = switch (seatClass.toUpperCase()) {
            case "ECONOMY" -> flight.getEconomyPrice() != null ? flight.getEconomyPrice() : flight.getBasePrice();
            case "BUSINESS" -> flight.getBusinessPrice() != null ? flight.getBusinessPrice() :
                    flight.getBasePrice().multiply(BigDecimal.valueOf(2));
            case "FIRST" -> flight.getFirstClassPrice() != null ? flight.getFirstClassPrice() :
                    flight.getBasePrice().multiply(BigDecimal.valueOf(3));
            default -> flight.getBasePrice();
        };

        return pricePerSeat.multiply(BigDecimal.valueOf(numberOfPassengers));
    }

    /**
     * Maps a Booking entity to a BookingResponse DTO.
     *
     * @param booking the booking entity to map
     * @return BookingResponse DTO
     */
    private BookingResponse mapToBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getFlight().getFlightNumber(),
                booking.getFlight().getDepartureAirport().getCode(),
                booking.getFlight().getArrivalAirport().getCode(),
                booking.getFlight().getDepartureTime(),
                booking.getFlight().getArrivalTime(),
                booking.getNumberOfPassengers(),
                booking.getTotalAmount(),
                booking.getStatus().name(),
                booking.getSeatClass().name(),
                booking.getUser().getEmail(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}