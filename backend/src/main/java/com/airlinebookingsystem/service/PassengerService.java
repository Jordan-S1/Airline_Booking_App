package com.airlinebookingsystem.service;

import com.airlinebookingsystem.dto.PassengerRequest;
import com.airlinebookingsystem.dto.PassengerResponse;
import com.airlinebookingsystem.entity.Booking;
import com.airlinebookingsystem.entity.Passenger;
import com.airlinebookingsystem.repository.BookingRepository;
import com.airlinebookingsystem.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing passenger-related operations in the airline booking system.
 * Provides methods for creating, retrieving, updating, and deleting passenger records.
 * All operations are transactional and interact with the PassengerRepository.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;

    /**
     * Creates a new passenger record associated with a booking.
     *
     * @param passengerRequest the passenger details to create
     * @param bookingId        the ID of the booking to associate with
     * @return PassengerResponse containing the created passenger details
     * @throws RuntimeException if a booking is not found
     */
    public PassengerResponse createPassenger(PassengerRequest passengerRequest, Long bookingId) {
        log.info("Creating passenger for booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Passenger passenger = Passenger.builder()
                .firstName(passengerRequest.firstName())
                .lastName(passengerRequest.lastName())
                .dateOfBirth(passengerRequest.dateOfBirth())
                .gender(Passenger.Gender.valueOf(passengerRequest.gender().toUpperCase()))
                .passportNumber(passengerRequest.passportNumber())
                .nationality(passengerRequest.nationality())
                .passengerType(passengerRequest.passengerType() != null ?
                              Passenger.PassengerType.valueOf(passengerRequest.passengerType().toUpperCase()) :
                              Passenger.PassengerType.ADULT)
                .booking(booking)
                .build();

        passenger = passengerRepository.save(passenger);
        log.info("Passenger created successfully with ID: {}", passenger.getId());

        return mapToPassengerResponse(passenger);
    }

    /**
     * Creates multiple passengers for a booking.
     *
     * @param passengerRequests list of passenger details to create
     * @param bookingId         the ID of the booking to associate with
     * @return List of PassengerResponse containing the created passengers
     */
    public List<PassengerResponse> createPassengers(List<PassengerRequest> passengerRequests, Long bookingId) {
        log.info("Creating {} passengers for booking: {}", passengerRequests.size(), bookingId);

        return passengerRequests.stream()
                .map(request -> createPassenger(request, bookingId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a passenger by their ID.
     *
     * @param id the ID of the passenger to retrieve
     * @return PassengerResponse containing the passenger details
     * @throws RuntimeException if the passenger is not found
     */
    public PassengerResponse getPassengerById(Long id) {
        log.info("Retrieving passenger with ID: {}", id);
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Passenger not found"));
        return mapToPassengerResponse(passenger);
    }

    /**
     * Retrieves all passengers associated with a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return List of PassengerResponse objects
     */
    public List<PassengerResponse> getPassengersByBookingId(Long bookingId) {
        log.info("Retrieving passengers for booking: {}", bookingId);
        List<Passenger> passengers = passengerRepository.findByBookingId(bookingId);
        return passengers.stream()
                .map(this::mapToPassengerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all passengers booked on a specific flight.
     *
     * @param flightId the ID of the flight
     * @return List of PassengerResponse objects
     */
    public List<PassengerResponse> getPassengersByFlightId(Long flightId) {
        log.info("Retrieving passengers for flight: {}", flightId);
        List<Passenger> passengers = passengerRepository.findByFlightId(flightId);
        return passengers.stream()
                .map(this::mapToPassengerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Finds passengers by their passport number.
     *
     * @param passportNumber the passport number to search for
     * @return List of PassengerResponse objects
     */
    public List<PassengerResponse> getPassengersByPassportNumber(String passportNumber) {
        log.info("Searching passengers by passport number: {}", passportNumber);
        List<Passenger> passengers = passengerRepository.findByPassportNumber(passportNumber);
        return passengers.stream()
                .map(this::mapToPassengerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates passenger information.
     *
     * @param id               the ID of the passenger to update
     * @param passengerRequest the updated passenger details
     * @return PassengerResponse containing the updated passenger details
     * @throws RuntimeException if the passenger is not found
     */
    public PassengerResponse updatePassenger(Long id, PassengerRequest passengerRequest) {
        log.info("Updating passenger with ID: {}", id);
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        passenger.setFirstName(passengerRequest.firstName());
        passenger.setLastName(passengerRequest.lastName());
        passenger.setDateOfBirth(passengerRequest.dateOfBirth());
        passenger.setGender(Passenger.Gender.valueOf(passengerRequest.gender().toUpperCase()));
        passenger.setPassportNumber(passengerRequest.passportNumber());
        passenger.setNationality(passengerRequest.nationality());
        
        if (passengerRequest.passengerType() != null) {
            passenger.setPassengerType(Passenger.PassengerType.valueOf(passengerRequest.passengerType().toUpperCase()));
        }

        passenger = passengerRepository.save(passenger);
        log.info("Passenger updated successfully with ID: {}", passenger.getId());

        return mapToPassengerResponse(passenger);
    }

    /**
     * Assigns a seat to a passenger.
     *
     * @param passengerId the ID of the passenger
     * @param seatNumber  the seat number to assign
     * @return PassengerResponse containing the updated passenger details
     * @throws RuntimeException if the passenger is not found
     */
    public PassengerResponse assignSeat(Long passengerId, String seatNumber) {
        log.info("Assigning seat {} to passenger: {}", seatNumber, passengerId);
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        passenger.setSeatNumber(seatNumber);
        passenger = passengerRepository.save(passenger);

        log.info("Seat assigned successfully to passenger: {}", passengerId);
        return mapToPassengerResponse(passenger);
    }

    /**
     * Removes a passenger from the system.
     *
     * @param id the ID of the passenger to delete
     * @throws RuntimeException if a passenger is not found
     */
    public void deletePassenger(Long id) {
        log.info("Deleting passenger with ID: {}", id);
        if (!passengerRepository.existsById(id)) {
            throw new RuntimeException("Passenger not found");
        }
        passengerRepository.deleteById(id);
        log.info("Passenger deleted successfully with ID: {}", id);
    }

    /**
     * Retrieves all passengers in the system.
     *
     * @return List of all PassengerResponse objects
     */
    public List<PassengerResponse> getAllPassengers() {
        log.info("Retrieving all passengers");
        List<Passenger> passengers = passengerRepository.findAll();
        return passengers.stream()
                .map(this::mapToPassengerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a Passenger entity to a PassengerResponse DTO.
     *
     * @param passenger the passenger entity to map
     * @return PassengerResponse DTO
     */
    private PassengerResponse mapToPassengerResponse(Passenger passenger) {
        return new PassengerResponse(
                passenger.getId(),
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getDateOfBirth(),
                passenger.getGender().name(),
                passenger.getPassportNumber(),
                passenger.getNationality(),
                passenger.getSeatNumber(),
                passenger.getPassengerType().name(),
                passenger.getBooking().getId(),
                passenger.getBooking().getBookingReference(),
                passenger.getBooking().getFlight().getFlightNumber(),
                passenger.getCreatedAt(),
                passenger.getUpdatedAt()
        );
    }
}