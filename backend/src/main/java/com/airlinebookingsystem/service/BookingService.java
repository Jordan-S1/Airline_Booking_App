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
        log.info("Creating booking for user {} and flight {}", userId, request.getFlightId());

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate flight
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        // Check seat availability
        if (flight.getAvailableSeats() < request.getPassengers().size()) {
            throw new RuntimeException("Insufficient seats available");
        }

        // Calculate total amount based on seat class
        BigDecimal totalAmount = calculateTotalAmount(flight, request.getSeatClass(), request.getPassengers().size());

        // Create a booking entity
        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .user(user)
                .flight(flight)
                .numberOfPassengers(request.getPassengers().size())
                .totalAmount(totalAmount)
                .status(Booking.BookingStatus.PENDING)
                .seatClass(Booking.SeatClass.valueOf(request.getSeatClass()))
                .build();

        // Save booking first to get ID
        booking = bookingRepository.save(booking);

        // Create passengers using PassengerService
        List<PassengerResponse> createdPassengers = passengerService.createPassengers(request.getPassengers(), booking.getId());
        log.info("Created {} passengers for booking {}", createdPassengers.size(), booking.getBookingReference());

        // Update flight availability
        flight.setAvailableSeats(flight.getAvailableSeats() - request.getPassengers().size());
        flightRepository.save(flight);

        log.info("Booking created successfully with reference: {}", booking.getBookingReference());
        return mapToBookingResponse(booking);
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

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

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
        flight.setAvailableSeats(flight.getAvailableSeats() + booking.getNumberOfPassengers());
        flightRepository.save(flight);

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
        BigDecimal pricePerSeat;
        
        switch (seatClass.toUpperCase()) {
            case "ECONOMY":
                pricePerSeat = flight.getEconomyPrice() != null ? flight.getEconomyPrice() : flight.getBasePrice();
                break;
            case "BUSINESS":
                pricePerSeat = flight.getBusinessPrice() != null ? flight.getBusinessPrice() : 
                              flight.getBasePrice().multiply(BigDecimal.valueOf(2));
                break;
            case "FIRST":
                pricePerSeat = flight.getFirstClassPrice() != null ? flight.getFirstClassPrice() : 
                              flight.getBasePrice().multiply(BigDecimal.valueOf(3));
                break;
            default:
                pricePerSeat = flight.getBasePrice();
        }
        
        return pricePerSeat.multiply(BigDecimal.valueOf(numberOfPassengers));
    }

    /**
     * Maps a Booking entity to a BookingResponse DTO.
     *
     * @param booking the booking entity to map
     * @return BookingResponse DTO
     */
    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setFlightNumber(booking.getFlight().getFlightNumber());
        response.setDepartureAirport(booking.getFlight().getDepartureAirport().getCode());
        response.setArrivalAirport(booking.getFlight().getArrivalAirport().getCode());
        response.setDepartureTime(booking.getFlight().getDepartureTime());
        response.setArrivalTime(booking.getFlight().getArrivalTime());
        response.setNumberOfPassengers(booking.getNumberOfPassengers());
        response.setTotalAmount(booking.getTotalAmount());
        response.setStatus(booking.getStatus().name());
        response.setSeatClass(booking.getSeatClass().name());
        response.setUserEmail(booking.getUser().getEmail());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        
        return response;
    }
}